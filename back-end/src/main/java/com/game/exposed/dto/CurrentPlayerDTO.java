package com.game.exposed.dto;

public class CurrentPlayerDTO {
    private String roomId;
    private String currentPlayerName;

    public CurrentPlayerDTO() {
    }

    public CurrentPlayerDTO(String roomId, String currentPlayerName) {
        this.roomId = roomId;
        this.currentPlayerName = currentPlayerName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public void setCurrentPlayerName(String currentPlayerName) {
        this.currentPlayerName = currentPlayerName;
    }
}