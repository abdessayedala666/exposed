package com.game.exposed.Controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.game.exposed.Service.GameService;
import com.game.exposed.Service.RoomService;
import com.game.exposed.Exceptions.MissingSessionDataException;
import com.game.exposed.Exceptions.ResourceNotFoundException;
import com.game.exposed.dto.MoveResult;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/room")
public class RoomController {

    private final RoomService roomService;
    private final GameService gameService;

    public RoomController(RoomService roomService, GameService gameService) {
        this.roomService = roomService;
        this.gameService = gameService;
    }
    
    @PostMapping("/create")
    public String createRoom(HttpSession session) {
        if (session == null) {
            throw new MissingSessionDataException("Session is invalid");
        }
        
        String sessionId = session.getId();
        String name = (String) session.getAttribute("name");
        
        if (sessionId == null || sessionId.isBlank()) {
            throw new MissingSessionDataException("Session ID is missing");
        }
        
        if (name == null || name.isBlank()) {
            throw new MissingSessionDataException("Username is not set in the session");
        }
        
        String roomId = roomService.createRoom(sessionId, name);
        return roomId;
    }
    
    @PostMapping("/me")
    public String join(HttpSession session) {
        if (session == null) {
            throw new MissingSessionDataException("Session is invalid");
        }
        
        String name = (String) session.getAttribute("name");

        if (name == null || name.isBlank()) {
            throw new MissingSessionDataException("Username is not set in the session");
        }

        return name;
    }

    @GetMapping("{roomId}")
    public String roomExistance(@PathVariable String roomId) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        
        boolean exits = roomService.roomExistance(roomId);
        if (exits) {
            return "true";
        }
        else{
            return "false";
        }
    }
    
    @GetMapping("{roomId}/seats")
    public List<String> getSeats(@PathVariable String roomId) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        
        List<String> seats = roomService.getSeats(roomId);
        if (seats == null) {
            throw new ResourceNotFoundException("Seats not found for room: " + roomId);
        }
        return seats;
    }
    
    @GetMapping("{roomId}/owner")
    public String getOwner(@PathVariable String roomId) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        
        String owner = roomService.getOwner(roomId);
        if (owner == null) {
            throw new ResourceNotFoundException("Room not found: " + roomId);
        }
        return owner;
    }

    @GetMapping("{roomId}/state")
    public ResponseEntity<MoveResult> getCurrentGameState(
            @PathVariable String roomId,
            HttpSession session) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        if (session == null) {
            throw new MissingSessionDataException("Session is invalid");
        }

        String name = (String) session.getAttribute("name");
        if (name == null || name.isBlank()) {
            throw new MissingSessionDataException("Username is not set in the session");
        }

        MoveResult state = gameService.getCurrentStateForPlayer(roomId, name);
        if (state == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(state);
    }
}