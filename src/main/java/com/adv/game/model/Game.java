package com.adv.game.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Game {

    private final GameRoom room;
    private List<Round> rounds = new ArrayList<>();
    private String winner;
    private boolean active;
    private HashMap<String, Integer> scores = new HashMap<>();

    public Game(GameRoom room) {
        this.room = room;
        this.active = true;
    }

    public GameRoom getRoom() {
        return room;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public HashMap<String, Integer> getScores() {
        return scores;
    }

    public void setScores(HashMap<String, Integer> scores) {
        this.scores = scores;
    }
}
