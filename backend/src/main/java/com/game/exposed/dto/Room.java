package com.game.exposed.dto;

import java.util.HashMap;
import java.util.Map;

import com.game.exposed.game.Components.Player;

public class Room {

    private final String id;
    private final String owner;
    private final Map<Integer, Player> seats = new HashMap<>();

    public Room(String id, String owner) {
        this.id = id;
        this.owner = owner;

        // initialize seat keys with empty assignments
        seats.put(0, null);
        seats.put(1, null);
        seats.put(2, null);
        seats.put(3, null);
    }

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public Map<Integer, Player> getSeats() {
        return seats;
    }
}