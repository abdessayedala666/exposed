package com.game.exposed.Service;

import org.springframework.stereotype.Service;

import com.game.exposed.models.Room;
import com.game.exposed.models.RoomSeat;
import com.game.exposed.Exceptions.ResourceNotFoundException;
import com.game.exposed.Repository.RoomRepository;
import com.game.exposed.Repository.RoomseatRepository;
import com.game.exposed.Exceptions.InvalidOperationException;
import com.game.exposed.game.Components.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomseatRepository roomseatRepository;
    public RoomService(RoomRepository roomRepository , RoomseatRepository roomseatRepository) {
        this.roomRepository = roomRepository;
        this.roomseatRepository = roomseatRepository;
    }

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    
    public String createRoom(String sessionId, String name) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        String roomId = generateUniqueRoomId();
        String owner = name + ":" + sessionId;
        Room room = new Room(roomId, owner);
        room = roomRepository.save(room);
        for ( int i = 0 ; i < 4 ; i++){
            RoomSeat seat = new RoomSeat(room , i);
            roomseatRepository.save(seat);
        }
        return roomId;
    }

    public String getOwner(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        
        Room room = roomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
        if (room == null) {
            throw new ResourceNotFoundException("Room not found: " + roomId);
        }
        
        String owner = room.getOwner();
        if (owner == null) {
            throw new ResourceNotFoundException("Room owner not found for room: " + roomId);
        }
        String ownerName = owner.split(":")[0];
        return ownerName;
    }
    
    public Map<String, Room> getRooms() {
        return this.rooms;
    }

    public synchronized void join(String roomId, String sessionId, String name, int index) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (index < 0 || index > 3) {
            throw new IllegalArgumentException("Invalid seat index: " + index);
        }
        
        Room room = roomRepository.findByRoomId(roomId).orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));


        if (room == null) {
            throw new ResourceNotFoundException("Room not found: " + roomId);
        }
        List<RoomSeat> seats = room.getSeats();
        if (seats == null) {
            throw new InvalidOperationException("Seats not initialized for room: " + roomId);
        }

        Player targetSeat = seats.get(index);
        if (targetSeat == null) {
            // null means the seat exists but is empty
        } else if (!sessionId.equals(targetSeat.getSessionId())) {
            throw new InvalidOperationException("Seat already taken");
        }

        for (int i = 0; i < 4; i++) {
            Player seat = seats.get(i);
            if (seat != null && sessionId.equals(seat.getSessionId())) {
                seats.put(i, null);
            }
        }
        seats.put(index, new Player(sessionId, name, index));
        System.out.println(room.getOwner());
    }

    public synchronized void leave(String roomId, String sessionId) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room not found: " + roomId);
        }
        
        Map<Integer, Player> seats = room.getSeats();
        if (seats == null) {
            throw new InvalidOperationException("Seats not initialized for room: " + roomId);
        }
        
        for (int i = 0; i < 4; i++) {
            Player seat = seats.get(i);
            if (seat != null && sessionId.equals(seat.getSessionId())) {
                seats.put(i, null);
                return;
            }
        }
    }
    
    public boolean roomExistance(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return false;
        }
        return rooms.containsKey(roomId);
    }

    public synchronized List<String> getSeats(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room not found: " + roomId);
        }
        
        Map<Integer, Player> seats = room.getSeats();
        if (seats == null) {
            throw new InvalidOperationException("Seats not initialized for room: " + roomId);
        }
        
        List<String> result = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Player seat = seats.get(i);
            if (seat == null) {
                result.add("Player" + (i + 1));
            } else {
                result.add(seat.getName());
            }
        }

        return result;
    }

    private String generateRoomId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();

        StringBuilder roomId = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            roomId.append(chars.charAt(random.nextInt(chars.length())));
        }
        return roomId.toString();
    }
    
    private String generateUniqueRoomId() {
        String roomId;

        do {
            roomId = generateRoomId();
        } while (rooms.containsKey(roomId));

        return roomId;
    }
}