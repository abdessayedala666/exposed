package com.game.exposed.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.game.exposed.Exceptions.InvalidOperationException;
import com.game.exposed.Exceptions.ResourceNotFoundException;

class RoomServiceTest {

    @Test
    void createRoomShouldCreateRoomWithEmptySeats() {
        RoomService roomService = new RoomService();

        String roomId = roomService.createRoom("session-1", "Alice");

        assertTrue(roomService.roomExistance(roomId));
        assertEquals("Alice", roomService.getOwner(roomId));
        assertEquals(List.of("Player1", "Player2", "Player3", "Player4"), roomService.getSeats(roomId));
    }

    @Test
    void createRoomShouldRejectBlankSessionId() {
        RoomService roomService = new RoomService();

        assertThrows(IllegalArgumentException.class, () -> roomService.createRoom(" ", "Alice"));
    }

    @Test
    void createRoomShouldRejectBlankName() {
        RoomService roomService = new RoomService();

        assertThrows(IllegalArgumentException.class, () -> roomService.createRoom("session-1", " "));
    }

    @Test
    void roomExistenceShouldReturnFalseForNullAndBlank() {
        RoomService roomService = new RoomService();

        assertFalse(roomService.roomExistance(null));
        assertFalse(roomService.roomExistance(" "));
    }

    @Test
    void getOwnerShouldThrowForUnknownRoom() {
        RoomService roomService = new RoomService();

        assertThrows(ResourceNotFoundException.class, () -> roomService.getOwner("missing"));
    }

    @Test
    void getSeatsShouldThrowForUnknownRoom() {
        RoomService roomService = new RoomService();

        assertThrows(ResourceNotFoundException.class, () -> roomService.getSeats("missing"));
    }

    @Test
    void joinShouldThrowForInvalidSeatIndex() {
        RoomService roomService = new RoomService();
        String roomId = roomService.createRoom("session-1", "Alice");

        assertThrows(IllegalArgumentException.class, () -> roomService.join(roomId, "session-2", "Bob", 4));
    }

    @Test
    void joinShouldPlacePlayerInRequestedSeat() {
        RoomService roomService = new RoomService();
        String roomId = roomService.createRoom("session-1", "Alice");

        roomService.join(roomId, "session-2", "Bob", 1);

        assertEquals(List.of("Player1", "Bob", "Player3", "Player4"), roomService.getSeats(roomId));
    }

    @Test
    void joinShouldRejectSeatTakenByAnotherSession() {
        RoomService roomService = new RoomService();
        String roomId = roomService.createRoom("session-1", "Alice");
        roomService.join(roomId, "session-2", "Bob", 1);

        InvalidOperationException exception = assertThrows(
                InvalidOperationException.class,
                () -> roomService.join(roomId, "session-3", "Carol", 1));

        assertEquals("Seat already taken", exception.getMessage());
    }

    @Test
    void joinShouldMoveSameSessionBetweenSeats() {
        RoomService roomService = new RoomService();
        String roomId = roomService.createRoom("session-1", "Alice");

        roomService.join(roomId, "session-2", "Bob", 1);
        roomService.join(roomId, "session-2", "Bob", 3);

        assertEquals(List.of("Player1", "Player2", "Player3", "Bob"), roomService.getSeats(roomId));
    }

    @Test
    void leaveShouldClearPlayerSeat() {
        RoomService roomService = new RoomService();
        String roomId = roomService.createRoom("session-1", "Alice");
        roomService.join(roomId, "session-2", "Bob", 1);

        roomService.leave(roomId, "session-2");

        assertEquals(List.of("Player1", "Player2", "Player3", "Player4"), roomService.getSeats(roomId));
    }
}