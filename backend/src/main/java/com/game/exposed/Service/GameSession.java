package com.game.exposed.Service;

import com.game.exposed.game.Components.GameEngineV2;

public class GameSession {
    private final String roomId;
    private final GameEngineV2 gameEngine;

    public GameSession(
            String roomId,
            GameEngineV2 gameEngine) {
        this.roomId = roomId;
        this.gameEngine = gameEngine;
    }

    public String getRoomId() {
        return roomId;
    }

    public GameEngineV2 getGameEngine() {
        return gameEngine;
    }
}