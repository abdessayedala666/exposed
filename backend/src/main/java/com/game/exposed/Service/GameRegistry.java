package com.game.exposed.Service;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class GameRegistry {
    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

    public void save(String roomId, GameSession gameSession) {
        sessions.put(roomId, gameSession);
    }

    public GameSession get(String roomId) {
        return sessions.get(roomId);
    }

    public boolean exists(String roomId) {
        return sessions.containsKey(roomId);
    }
}