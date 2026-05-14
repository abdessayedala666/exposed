package com.game.exposed.Controllers;

import com.game.exposed.Service.GameMessagingLayer;
import com.game.exposed.Service.GameService;
import com.game.exposed.Service.RoomService;
import com.game.exposed.Service.Handler.LaunchGameHandler;
import com.game.exposed.Service.Handler.PlayTurnHandler;
import com.game.exposed.Service.Handler.UpdateSeatsHandler;
import com.game.exposed.dto.MessageDTO;
import com.game.exposed.dto.MoveResult;
import com.game.exposed.dto.MoveRequestDto;
import com.game.exposed.dto.SeatAction;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class RoomSocketController {
    private GameMessagingLayer gameMessagingLayer ;
    private final GameService gameService;
    private final PlayTurnHandler playTurnValidationHandler;
    private final LaunchGameHandler launchGameHandler;
    private final UpdateSeatsHandler updateSeatsHandler;
    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomSocketController(
            RoomService roomService,
            SimpMessagingTemplate messagingTemplate, GameService gameService,
        PlayTurnHandler playTurnValidationHandler,
        LaunchGameHandler launchGameHandler,
        UpdateSeatsHandler updateSeatsHandler,
        GameMessagingLayer gameMessagingLayer ) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
        this.playTurnValidationHandler = playTurnValidationHandler;
        this.launchGameHandler = launchGameHandler;
        this.updateSeatsHandler = updateSeatsHandler;
        this.gameMessagingLayer = gameMessagingLayer;
    }

    @MessageMapping("/room/{roomId}/seats")
    public void updateSeats(
            @DestinationVariable String roomId,
            @Payload SeatAction payload,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String currentUser = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;
        try {
            List<String> seats = updateSeatsHandler.handleUpdateSeats(roomId, payload, headerAccessor);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, seats);
        } catch (RuntimeException ex) {
            System.out.println("Seat update rejected for user " + currentUser + ": " + ex.getMessage());
            gameMessagingLayer.sendRoomError(currentUser, ex.getMessage());
        }
    }
    
    @MessageMapping("/room/{roomId}/launch")
    public Map<String, MoveResult> handleMessage(
            @DestinationVariable String roomId,
            @Payload MessageDTO message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String currentUser = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;
        try {
            return gameMessagingLayer.sendLaunchSnapshots(launchGameHandler.handleLaunchGame(headerAccessor, roomId, message));
        } catch (RuntimeException ex) {
            System.out.println("Launch rejected for user " + currentUser + ": " + ex.getMessage());
            gameMessagingLayer.sendLaunchError(currentUser, ex.getMessage());
            return Map.of();
        }
    }

    @MessageMapping("/room/{roomId}/move")
    public void handleMove(
            @DestinationVariable String roomId,
            @Payload MoveRequestDto moveRequest,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        System.out.println("===== move received =====");
        System.out.println("roomId = " + roomId);
        System.out.println("user = " + (headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "null"));
        System.out.println("payload = " + moveRequest);

        String currentUser = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : null;

        try {
            gameMessagingLayer.sendMoveSnapshots(playTurnValidationHandler.handlePlayTurn(headerAccessor, roomId, moveRequest));
        } catch (RuntimeException ex) {
            System.out.println("Move rejected for user " + currentUser + ": " + ex.getMessage());
            gameMessagingLayer.sendMoveError(currentUser, ex.getMessage());
        }
    }
    
}

