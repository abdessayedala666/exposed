package com.game.exposed.Service.Handler;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.game.exposed.Exceptions.InvalidOperationException;
import com.game.exposed.Exceptions.ResourceNotFoundException;
import com.game.exposed.Service.RoomService;
import com.game.exposed.Service.GameService;
import com.game.exposed.dto.MessageDTO;
import com.game.exposed.dto.MoveResult;
import com.game.exposed.dto.Room;
import com.game.exposed.game.Components.Player;

@Service
public class LaunchGameHandler {
    private final GameService gameService;
    private final RoomService roomService;

    public LaunchGameHandler(GameService gameService, RoomService roomService) {
        this.gameService = gameService;
        this.roomService = roomService;
    }

    public Map<String, MoveResult> handleLaunchGame(
            SimpMessageHeaderAccessor headerAccessor,
            String roomId,
            MessageDTO message) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        if (message == null) {
            throw new IllegalArgumentException("Launch payload cannot be null");
        }
        if (message.getType() == null || message.getType().isBlank()) {
            throw new IllegalArgumentException("Launch type cannot be null or empty");
        }
        if (!"LAUNCH_GAME".equals(message.getType())) {
            throw new IllegalArgumentException("incorrect command");
        }

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        String name = null;
        if (sessionAttributes != null) {
            Object idObj = sessionAttributes.get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
            Object nameObj = sessionAttributes.get("name");
            if (idObj == null || idObj.toString().isBlank()) {
                throw new IllegalArgumentException("Session ID cannot be null or empty");
            }
            name = nameObj != null ? nameObj.toString() : null;
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }

        Room room = roomService.getRooms().get(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room not found: " + roomId);
        }

        String roomOwner = room.getRoomOwner();
        if (roomOwner == null) {
            throw new InvalidOperationException("Room owner is not set for room: " + roomId);
        }
        if (!roomOwner.equals(name)) {
            throw new InvalidOperationException("this isn't the room Owner, can't launch the game");
        }

        Map<Integer, Player> players = room.getSeats();
        if (players == null) {
            throw new InvalidOperationException("Players/seats not initialized for room: " + roomId);
        }
        for (int i = 0; i < 4; i++) {
            if (players.get(i) == null) {
                throw new InvalidOperationException("Seat " + i + " is not initialized");
            }
        }

        return gameService.launchGame(roomId, message.getType());
    }
}