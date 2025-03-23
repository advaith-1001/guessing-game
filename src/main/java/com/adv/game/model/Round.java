package com.adv.game.model;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;


@Setter
@Getter
public class Round {
    private boolean active;
    private Game game;
    private String winner;
    private Country country;
    private int number;

    public Round() {
        this.active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
