package com.adv.game.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRoom {

    private final String roomCode;
    private final String host;
    private final List<String> players = new ArrayList<>();
    private final Map<String, Integer> scores = new HashMap<>();

    public GameRoom(String roomCode, String host) {
        this.roomCode = roomCode;
        this.host = host;
        players.add(host);
        scores.put(host, 0);
    }

    public boolean addPlayer(String player) {
        if(players.size() >= 5) return false;
        players.add(player);
        scores.put(player, 0);
        return true;
    }



    public String getRoomCode() {
        return roomCode;
    }

    public String getHost() {
        return host;
    }

    public List<String> getPlayers() {
        return players;
    }

    public Map<String, Integer> getScores() {
        return scores;
    }
}
