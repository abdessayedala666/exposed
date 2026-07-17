package com.game.exposed.Service.Handler;

import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.game.exposed.Exceptions.InvalidOperationException;
import com.game.exposed.Exceptions.MissingSessionDataException;
import com.game.exposed.Service.RoomService;
import com.game.exposed.dto.SeatAction;

@Service
public class UpdateSeatsHandler {
    private final RoomService roomService;

    public UpdateSeatsHandler(RoomService roomService) {
        this.roomService = roomService;
    }

    public List<String> handleUpdateSeats(
            String roomId,
            SeatAction payload,
            SimpMessageHeaderAccessor headerAccessor) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        if (payload == null) {
            throw new IllegalArgumentException("Seat action payload cannot be null");
        }

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        String sessionId = null;
        String name = null;

        if (sessionAttributes != null) {
            Object idObj = sessionAttributes.get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
            Object nameObj = sessionAttributes.get("name");
            sessionId = idObj != null ? idObj.toString() : null;
            name = nameObj != null ? nameObj.toString() : null;
        }

        if (sessionId == null || sessionId.isBlank()) {
            throw new MissingSessionDataException("Session ID is missing");
        }
        if (name == null || name.isBlank()) {
            throw new MissingSessionDataException("User name is missing from session");
        }

        String action = payload.getAction();
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Action cannot be null or empty");
        }

        if ("join".equals(action)) {
            roomService.join(roomId, sessionId, name, payload.getIndex());
        } else if ("leave".equals(action)) {
            roomService.leave(roomId, sessionId);
        } else {
            throw new IllegalArgumentException("Invalid action: " + action);
        }

        List<String> seats = roomService.getSeats(roomId);
        if (seats == null) {
            throw new InvalidOperationException("Failed to retrieve seats for room: " + roomId);
        }
        return seats;
    }
}