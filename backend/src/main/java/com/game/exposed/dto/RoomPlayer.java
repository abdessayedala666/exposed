package com.game.exposed.dto;

public class RoomPlayer {
    private final String userId;
    private final String username;

    public RoomPlayer(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}