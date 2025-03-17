package com.adv.game.model;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;


@Setter
@Getter
public class Round {
    private final boolean active;
    private final GameRoom room;
    private final String winner;

    public Round(boolean active, GameRoom room, String winner) {
        this.active = active;
        this.room = room;
        this.winner = winner;
    }

}
