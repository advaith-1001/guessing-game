package com.adv.game;

import com.adv.game.model.*;
import com.adv.game.service.FlagService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.util.concurrent.ScheduledFuture;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameWebSocketHandler extends TextWebSocketHandler {

    private final GameManager gameManager = new GameManager();
    private final Map<WebSocketSession, String> playerSessions = new HashMap<>();
    String gameWinner = null;


        FlagService flagService = new FlagService();
        long startTime = System.currentTimeMillis();
        List<Country> countries = flagService.fetchAllFlags();
        long endTime = System.currentTimeMillis();

    Random random = new Random();


    java.util.concurrent.ScheduledFuture currentRoundTask;
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(payload);

        String type = jsonNode.get("type").asText();
        String player = jsonNode.get("player").asText();

        if(type.equals("CREATE_ROOM")) {
            String roomCode = gameManager.createRoom(player);
            playerSessions.put(session, roomCode);
            session.sendMessage(new TextMessage("{ROOM_CREATED, ROOMCODE: " + roomCode + "}"));
            System.out.println(player + " CREATED THE ROOM " + roomCode);
        } else if (type.equals("JOIN_ROOM")) {
            String roomCode = jsonNode.get("roomCode").asText();
            boolean success = gameManager.joinRoom(roomCode, player);
            if (success) {
                playerSessions.put(session, roomCode);
                session.sendMessage(new TextMessage("{JOIN_SUCCESS, ROOMCODE: " + roomCode+  "}"));
                System.out.println(player + " JOINED THE ROOM " + roomCode);
                System.out.println("Players in the room " + roomCode + " are " + gameManager.getRoom(roomCode).getPlayers());
                broadcastMessage(player + " JOINED THE ROOM " + roomCode);
            } else {
                session.sendMessage(new TextMessage("{JOIN_FAILED}"));
            }
        } else if (type.equals("START_GAME")) {
            String roomCode = jsonNode.get("roomCode").asText();
            GameRoom room = gameManager.getRoom(roomCode);
            gameWinner = "";
            if(room.getRunning()) {
                session.sendMessage(new TextMessage("Game already running"));
                return;
            }
            System.out.println("API Response Time: " + (endTime - startTime) + " ms");
            System.out.println("Starting game for roomcode: " + roomCode);
            broadcastMessage("Game is starting for roomcode: " + roomCode);



            startGame(roomCode, countries);

        } else if (type.equals("SUBMIT_ANSWER")) {
            String roomCode = jsonNode.get("roomCode").asText();
            GameRoom room = gameManager.getRoom(roomCode);
            String playerName = jsonNode.get("player").asText();
            String submittedAnswer = jsonNode.get("answer").asText();

            try {
                if (room != null) {
                    submitAnswer(roomCode, playerName, submittedAnswer);
                } else {
                    broadcastMessage("ERROR SUBMITTING ANSWER");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startGame(String roomCode, List<Country> countries) {
        GameRoom room = gameManager.getRoom(roomCode);
        room.setRunning(true);

        Game newGame = new Game(room);
        room.getGames().add(newGame);

        for(String player: room.getPlayers()) {
            newGame.getScores().put(player, 0);
        }

        System.out.println(newGame.getScores());

        Runnable roundTask = new Runnable() {

            int round = 1;

            @Override
            public void run() {
                try {
                    if(round > 5) {
                        scheduler.shutdown();
                        broadcastMessage("GAME OVER!");
                        newGame.setActive(false);
                        broadcastMessage("The winner is: " + gameWinner);
                        return;
                    }

                    System.out.println("The round " + round + "has started");
                    Country country = countries.get(random.nextInt(countries.size()));

                    Round currRound = new Round();
                    currRound.setNumber(round);
                    currRound.setGame(newGame);
                    currRound.setCountry(country);
                    newGame.getRounds().add(currRound);
                    System.out.println(currRound.getCountry().getName());
                    for(String player: newGame.getScores().keySet()) {
                        if(gameWinner != null || newGame.getScores().get(player) > newGame.getScores().get(gameWinner)) {
                            gameWinner = player;
                        }
                    }

                    broadcastMessage("Round: " + currRound.getNumber() + " Guess the flag: " + currRound.getCountry().getFlagUrl());
                    round++;
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };

        currentRoundTask = scheduler.scheduleAtFixedRate(roundTask, 0, 20, TimeUnit.SECONDS);

    }

    public void broadcastMessage(String message) {
        for (WebSocketSession session : playerSessions.keySet()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void submitAnswer(String roomCode, String player, String answer  ) {
        GameRoom room = gameManager.getRoom(roomCode);
        if (room == null || !room.getRunning()) return;


        Game currentGame = room.getGames().getLast();
        Round currentRound = currentGame.getRounds().getLast();

        String correctCountryName = currentRound.getCountry().getName();

        if (correctCountryName.equalsIgnoreCase(answer)) {
            broadcastMessage(player + " has guessed the right answer: " + correctCountryName);
            currentRound.setActive(false);
            currentRound.setWinner(player);
            currentGame.getScores().put(player, currentGame.getScores().get(player) + 1);
            System.out.println(currentGame.getScores());

            if(currentGame.getRounds().size() >= 5 && currentGame.isActive()) {
                broadcastMessage("GAME OVER!");
                currentGame.setActive(false);
                System.out.println(currentGame.getScores());
                scheduler.shutdown();
                return;
            }

            if (currentRoundTask != null && !currentRoundTask.isCancelled()) {
                currentRoundTask.cancel(false);
            }

            scheduler.schedule(() -> startNextRound(roomCode, currentGame), 3, TimeUnit.SECONDS);

            currentRoundTask = scheduler.scheduleAtFixedRate(() -> {
                startNextRound(roomCode, currentGame);
            },10, 20, TimeUnit.SECONDS );
            
        }
    }
    private void startNextRound(String roomCode, Game game) {



        if (game.getRounds().size() >= 5 && game.isActive()) {
            broadcastMessage("GAME OVER!");
            game.setActive(false);
            System.out.println(game.getScores());
            broadcastMessage("The winner is: " + gameWinner);
            scheduler.shutdown();
            return;
        }

        Country country = countries.get(random.nextInt(countries.size()));

        Round newRound = new Round();
        newRound.setNumber(game.getRounds().size() + 1);
        newRound.setGame(game);
        newRound.setCountry(country);
        game.getRounds().add(newRound);

        for(String player: game.getScores().keySet()) {
            if(gameWinner != null || game.getScores().get(player) > game.getScores().get(gameWinner)) {
                gameWinner = player;
            }
        }


        broadcastMessage("Round: " + newRound.getNumber() + " Guess the flag: " + newRound.getCountry().getFlagUrl());

    }


}
