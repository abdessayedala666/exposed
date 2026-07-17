package com.game.exposed.Service.Handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import com.game.exposed.Exceptions.ResourceNotFoundException;

import com.game.exposed.Exceptions.InvalidOperationException;
import com.game.exposed.Service.GameService;
import com.game.exposed.Service.RoomService;
import com.game.exposed.dto.Room;
import com.game.exposed.dto.MessageDTO;
import com.game.exposed.dto.MoveResult;

class LaunchGameHandlerTest {

    @Test
    void ownerShouldBeAbleToLaunchGame() {
        RoomService roomService = new RoomService();
        String roomId = roomService.createRoom("owner-session", "Alice");
        roomService.join(roomId, "owner-session", "Alice", 0);
        roomService.join(roomId, "session-2", "Bob", 1);
        roomService.join(roomId, "session-3", "Carol", 2);
        roomService.join(roomId, "session-4", "Dave", 3);

        GameService gameService = mock(GameService.class);
        when(gameService.launchGame(roomId, "LAUNCH_GAME")).thenReturn(Map.of());

        LaunchGameHandler handler = new LaunchGameHandler(gameService, roomService);
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME, "owner-session");
        sessionAttributes.put("name", "Alice");
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);

        MessageDTO message = new MessageDTO();
        message.setType("LAUNCH_GAME");

        Map<String, MoveResult> result = handler.handleLaunchGame(headerAccessor, roomId, message);

        assertEquals(Map.of(), result);
        verify(gameService).launchGame(roomId, "LAUNCH_GAME");
    }

    @Test
    void shouldRejectBlankRoomId() {
        LaunchGameHandler handler = new LaunchGameHandler(mock(GameService.class), new RoomService());
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession("owner-session", "Alice");

        MessageDTO message = launchMessage();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.handleLaunchGame(headerAccessor, " ", message));

        assertEquals("Room ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldRejectNullMessage() {
        LaunchGameHandler handler = new LaunchGameHandler(mock(GameService.class), new RoomService());
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession("owner-session", "Alice");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.handleLaunchGame(headerAccessor, "room-1", null));

        assertEquals("Launch payload cannot be null", exception.getMessage());
    }

    @Test
    void shouldRejectBlankMessageType() {
        LaunchGameHandler handler = new LaunchGameHandler(mock(GameService.class), new RoomService());
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession("owner-session", "Alice");

        MessageDTO message = new MessageDTO();
        message.setType(" ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.handleLaunchGame(headerAccessor, "room-1", message));

        assertEquals("Launch type cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidMessageType() {
        LaunchGameHandler handler = new LaunchGameHandler(mock(GameService.class), new RoomService());
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession("owner-session", "Alice");

        MessageDTO message = new MessageDTO();
        message.setType("NOT_A_LAUNCH");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.handleLaunchGame(headerAccessor, "room-1", message));

        assertEquals("incorrect command", exception.getMessage());
    }

    @Test
    void shouldRejectMissingSessionId() {
        LaunchGameHandler handler = new LaunchGameHandler(mock(GameService.class), new RoomService());
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession(null, "Alice");

        MessageDTO message = launchMessage();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.handleLaunchGame(headerAccessor, "room-1", message));

        assertEquals("Session ID cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldRejectMissingPlayerName() {
        LaunchGameHandler handler = new LaunchGameHandler(mock(GameService.class), new RoomService());
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession("owner-session", null);

        MessageDTO message = launchMessage();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.handleLaunchGame(headerAccessor, "room-1", message));

        assertEquals("Player name cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldRejectRoomNotFound() {
        LaunchGameHandler handler = new LaunchGameHandler(mock(GameService.class), new RoomService());
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession("owner-session", "Alice");

        MessageDTO message = launchMessage();

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> handler.handleLaunchGame(headerAccessor, "missing-room", message));

        assertEquals("Room not found: missing-room", exception.getMessage());
    }

    @Test
    void shouldRejectRoomWithoutOwner() throws Exception {
        RoomService roomService = new RoomService();
        String roomId = roomService.createRoom("owner-session", "Alice");
        roomService.join(roomId, "owner-session", "Alice", 0);
        roomService.join(roomId, "session-2", "Bob", 1);
        roomService.join(roomId, "session-3", "Carol", 2);
        roomService.join(roomId, "session-4", "Dave", 3);
        setField(roomService.getRooms().get(roomId), "roomOwner", null);

        LaunchGameHandler handler = new LaunchGameHandler(mock(GameService.class), roomService);
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession("owner-session", "Alice");

        MessageDTO message = launchMessage();

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> handler.handleLaunchGame(headerAccessor, roomId, message));

        assertEquals("Room owner is not set for room: " + roomId, exception.getMessage());
    }

    @Test
    void shouldRejectRoomWithMissingSeats() throws Exception {
        RoomService roomService = new RoomService();
        String roomId = roomService.createRoom("owner-session", "Alice");
        roomService.join(roomId, "owner-session", "Alice", 0);
        roomService.join(roomId, "session-2", "Bob", 1);
        roomService.join(roomId, "session-3", "Carol", 2);
        roomService.join(roomId, "session-4", "Dave", 3);
        setField(roomService.getRooms().get(roomId), "seats", null);

        LaunchGameHandler handler = new LaunchGameHandler(mock(GameService.class), roomService);
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession("owner-session", "Alice");

        MessageDTO message = launchMessage();

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> handler.handleLaunchGame(headerAccessor, roomId, message));

        assertEquals("Players/seats not initialized for room: " + roomId, exception.getMessage());
    }

    @Test
    void ownerShouldBeAbleToLaunchOnlyWhenRoomIsFullySetUp() {
        RoomService roomService = new RoomService();
        String roomId = roomService.createRoom("owner-session", "Alice");
        roomService.join(roomId, "owner-session", "Alice", 0);
        roomService.join(roomId, "session-2", "Bob", 1);
        roomService.join(roomId, "session-3", "Carol", 2);
        roomService.join(roomId, "session-4", "Dave", 3);

        GameService gameService = mock(GameService.class);
        when(gameService.launchGame(roomId, "LAUNCH_GAME")).thenReturn(Map.of());

        LaunchGameHandler handler = new LaunchGameHandler(gameService, roomService);
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession("owner-session", "Alice");

        Map<String, MoveResult> result = handler.handleLaunchGame(headerAccessor, roomId, launchMessage());

        assertEquals(Map.of(), result);
        verify(gameService).launchGame(roomId, "LAUNCH_GAME");
    }

    @Test
    void nonOwnerShouldBeRejectedWithLaunchMessage() {
        RoomService roomService = new RoomService();
        String roomId = roomService.createRoom("owner-session", "Alice");

        GameService gameService = mock(GameService.class);
        LaunchGameHandler handler = new LaunchGameHandler(gameService, roomService);
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME, "other-session");
        sessionAttributes.put("name", "Bob");
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);

        MessageDTO message = new MessageDTO();
        message.setType("LAUNCH_GAME");

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> handler.handleLaunchGame(headerAccessor, roomId, message));

        assertEquals("this isn't the room Owner, can't launch the game", exception.getMessage());
    }

    private static MessageDTO launchMessage() {
        MessageDTO message = new MessageDTO();
        message.setType("LAUNCH_GAME");
        return message;
    }

    private static SimpMessageHeaderAccessor headerAccessorWithSession(String sessionId, String name) {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        Map<String, Object> sessionAttributes = new HashMap<>();
        if (sessionId != null) {
            sessionAttributes.put(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME, sessionId);
        }
        if (name != null) {
            sessionAttributes.put("name", name);
        }
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
        return headerAccessor;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}