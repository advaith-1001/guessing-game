package com.adv.game.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class GameManager {
    private final Map<String, GameRoom> rooms = new HashMap<>();

    public String createRoom(String host) {
        String roomCode = UUID.randomUUID().toString().substring(0,6);
        rooms.put(roomCode, new GameRoom(roomCode, host));
        return roomCode;
    }

    public boolean joinRoom(String roomCode, String player) {
        if(!rooms.containsKey(roomCode)) return false;
        return rooms.get(roomCode).addPlayer(player);
    }

    public GameRoom getRoom(String roomCode) {
        return rooms.get(roomCode);
    }


}
