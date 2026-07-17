package com.game.exposed.Service.Handler;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.game.exposed.Exceptions.ResourceNotFoundException;
import com.game.exposed.Service.GameRegistry;
import com.game.exposed.Service.GameService;
import com.game.exposed.Service.GameSession;
import com.game.exposed.dto.MoveResult;
import com.game.exposed.dto.MoveRequestDto;
import com.game.exposed.game.Components.CaptureType;
import com.game.exposed.game.Components.Card;
import com.game.exposed.game.Components.GameEngineV2;
import com.game.exposed.game.Components.Player;
import com.game.exposed.game.Components.Suit;

@Service
public class PlayTurnHandler {
    private final GameService gameService;
    private final GameRegistry gameRegistry;

    public PlayTurnHandler(GameService gameService, GameRegistry gameRegistry) {
        this.gameService = gameService;
        this.gameRegistry = gameRegistry;
    }

    public Map<String, MoveResult> handlePlayTurn(
            SimpMessageHeaderAccessor headerAccessor,
            String roomId,
            MoveRequestDto moveRequest) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        if (moveRequest == null) {
            throw new IllegalArgumentException("Move payload cannot be null");
        }
        if (moveRequest.getCard() == null) {
            throw new IllegalArgumentException("Payload card must be an object");
        }
        if (moveRequest.getCard().getNumber() == null) {
            throw new IllegalArgumentException("Card number must be numeric");
        }
        if (moveRequest.getCard().getSuit() == null || moveRequest.getCard().getSuit().isBlank()) {
            throw new IllegalArgumentException("Card suit is required");
        }
        if (moveRequest.getAction() == null || moveRequest.getAction().isBlank()) {
            throw new IllegalArgumentException("Move action is required");
        }

        Card card;
        try {
            card = new Card(
                    moveRequest.getCard().getNumber(),
                    Suit.valueOf(moveRequest.getCard().getSuit().trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid card suit: " + moveRequest.getCard().getSuit());
        }

        CaptureType captureType;
        try {
            captureType = CaptureType.valueOf(moveRequest.getAction().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid move action: " + moveRequest.getAction());
        }

        GameSession gameSession = gameRegistry.get(roomId);
        if (gameSession == null) {
            throw new ResourceNotFoundException("No active game session for room: " + roomId);
        }
        GameEngineV2 gameEngine = gameSession.getGameEngine();

        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        String sessionId = null;
        if (sessionAttributes != null) {
            Object idObj = sessionAttributes.get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
            sessionId = idObj != null ? idObj.toString() : null;
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }

        Player playingPlayer = gameEngine.getPlayerBySession(sessionId);
        if (playingPlayer == null) {
            throw new ResourceNotFoundException("Player not found for session: " + sessionId);
        }

        if (captureType == CaptureType.CAPTURE_FROM_OPPONENT) {
            if (moveRequest.getFacingSeat() == null) {
                throw new IllegalArgumentException("Facing seat is required for CAPTURE_FROM_OPPONENT");
            }
            Player facingPlayer = gameEngine.getPlayerByIndex(moveRequest.getFacingSeat());
            if (facingPlayer == null) {
                throw new ResourceNotFoundException("Facing player not found for seat: " + moveRequest.getFacingSeat());
            }
        }

        Player resolvedFacingPlayer = null;
        if (captureType == CaptureType.CAPTURE_FROM_OPPONENT) {
            resolvedFacingPlayer = gameEngine.getPlayerByIndex(moveRequest.getFacingSeat());
        }

        return gameService.playTurn(roomId, gameSession, playingPlayer, resolvedFacingPlayer, captureType, card);
    }
}