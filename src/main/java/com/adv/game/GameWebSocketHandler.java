package com.adv.game;

import com.adv.game.model.GameManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameWebSocketHandler extends TextWebSocketHandler {

    private final GameManager gameManager = new GameManager();
    private final Map<WebSocketSession, String> playerSessions = new HashMap<>();


    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(payload);

        String type = jsonNode.get("type").asText();
        String player = jsonNode.get("player").asText();

        if(type.equals("CREATE_ROOM")) {
            String roomCode = gameManager.createRoom(player);
            playerSessions.put(session, roomCode);
            session.sendMessage(new TextMessage("{ROOM_CREATED, ROOMCODE: }" + roomCode));
            System.out.println(player + " CREATED THE ROOM " + roomCode);
        } else if (type.equals("JOIN_ROOM")) {
            String roomCode = jsonNode.get("roomCode").asText();
            boolean success = gameManager.joinRoom(roomCode, player);
            if (success) {
                playerSessions.put(session, roomCode);
                session.sendMessage(new TextMessage("{JOIN_SUCCESS, ROOMCODE: }" + roomCode));
                System.out.println(player + " JOINED THE ROOM " + roomCode);
                System.out.println("Players in the room " + roomCode + " are " + gameManager.getRoom(roomCode).getPlayers());
                broadcastMessage(player + " JOINED THE ROOM " + roomCode);
            } else {
                session.sendMessage(new TextMessage("{JOIN_FAILED}"));
            }
        }
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
}
