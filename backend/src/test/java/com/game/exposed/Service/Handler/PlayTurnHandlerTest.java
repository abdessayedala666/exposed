package com.game.exposed.Service.Handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.game.exposed.Exceptions.ResourceNotFoundException;
import com.game.exposed.Service.GameRegistry;
import com.game.exposed.Service.GameService;
import com.game.exposed.Service.GameSession;
import com.game.exposed.Service.RoomService;
import com.game.exposed.dto.MoveRequestDto;
import com.game.exposed.dto.MoveResult;
import com.game.exposed.game.Components.CaptureType;
import com.game.exposed.game.Components.Card;
import com.game.exposed.game.Components.GameEngineV2;
import com.game.exposed.game.Components.Player;
import com.game.exposed.game.Components.Suit;

class PlayTurnHandlerTest {

    // Verifies a valid pile move is delegated to GameService with the current player.
    @Test
    void shouldDelegatePileMoveToGameService() {
        PlayTurnTestContext context = launchPlayableGame();
        Player currentPlayer = context.engine.getCurrentPlayer();
        Card chosenCard = currentPlayer.getHand().get(0);

        MoveRequestDto moveRequest = moveRequest(chosenCard.getNumber(), chosenCard.getSuit().name(), "CAPTURE_FROM_PILE", null);
        when(context.gameService.playTurn(
                eq(context.roomId),
                eq(context.session),
                eq(currentPlayer),
                eq(null),
                eq(CaptureType.CAPTURE_FROM_PILE),
            argThat(card -> card.getNumber() == chosenCard.getNumber() && card.getSuit() == chosenCard.getSuit()))).thenReturn(Map.of());

        Map<String, MoveResult> result = context.handler.handlePlayTurn(context.headerAccessor, context.roomId, moveRequest);

        assertEquals(Map.of(), result);
        verify(context.gameService).playTurn(
                eq(context.roomId),
                eq(context.session),
                eq(currentPlayer),
                eq(null),
                eq(CaptureType.CAPTURE_FROM_PILE),
            argThat(card -> card.getNumber() == chosenCard.getNumber() && card.getSuit() == chosenCard.getSuit()));
    }

    // Verifies a valid opponent move is delegated with the resolved facing player.
    @Test
    void shouldDelegateOpponentMoveWithFacingPlayer() {
        PlayTurnTestContext context = launchPlayableGame();
        Player currentPlayer = context.engine.getCurrentPlayer();
        Player facingPlayer = context.engine.getPlayerByIndex(2);
        Card chosenCard = currentPlayer.getHand().get(0);

        MoveRequestDto moveRequest = moveRequest(chosenCard.getNumber(), chosenCard.getSuit().name(), "CAPTURE_FROM_OPPONENT", 2);
        when(context.gameService.playTurn(
                eq(context.roomId),
                eq(context.session),
                eq(currentPlayer),
                eq(facingPlayer),
                eq(CaptureType.CAPTURE_FROM_OPPONENT),
            argThat(card -> card.getNumber() == chosenCard.getNumber() && card.getSuit() == chosenCard.getSuit()))).thenReturn(Map.of());

        Map<String, MoveResult> result = context.handler.handlePlayTurn(context.headerAccessor, context.roomId, moveRequest);

        assertEquals(Map.of(), result);
        verify(context.gameService).playTurn(
                eq(context.roomId),
                eq(context.session),
                eq(currentPlayer),
                eq(facingPlayer),
                eq(CaptureType.CAPTURE_FROM_OPPONENT),
            argThat(card -> card.getNumber() == chosenCard.getNumber() && card.getSuit() == chosenCard.getSuit()));
    }

    // Verifies blank room ids are rejected.
    @Test
    void shouldRejectBlankRoomId() {
        PlayTurnTestContext context = launchPlayableGame();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context.handler.handlePlayTurn(context.headerAccessor, " ", validPileMove(context)));

        assertEquals("Room ID cannot be null or empty", exception.getMessage());
    }

    // Verifies null move payloads are rejected.
    @Test
    void shouldRejectNullMoveRequest() {
        PlayTurnTestContext context = launchPlayableGame();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context.handler.handlePlayTurn(context.headerAccessor, context.roomId, null));

        assertEquals("Move payload cannot be null", exception.getMessage());
    }

    // Verifies missing card objects are rejected.
    @Test
    void shouldRejectMissingCard() {
        PlayTurnTestContext context = launchPlayableGame();
        MoveRequestDto moveRequest = new MoveRequestDto();
        moveRequest.setAction("CAPTURE_FROM_PILE");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context.handler.handlePlayTurn(context.headerAccessor, context.roomId, moveRequest));

        assertEquals("Payload card must be an object", exception.getMessage());
    }

    // Verifies missing card numbers are rejected.
    @Test
    void shouldRejectMissingCardNumber() {
        PlayTurnTestContext context = launchPlayableGame();
        MoveRequestDto moveRequest = moveRequest(null, "HEARTS", "CAPTURE_FROM_PILE", null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context.handler.handlePlayTurn(context.headerAccessor, context.roomId, moveRequest));

        assertEquals("Card number must be numeric", exception.getMessage());
    }

    // Verifies missing card suits are rejected.
    @Test
    void shouldRejectMissingCardSuit() {
        PlayTurnTestContext context = launchPlayableGame();
        MoveRequestDto moveRequest = moveRequest(1, " ", "CAPTURE_FROM_PILE", null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context.handler.handlePlayTurn(context.headerAccessor, context.roomId, moveRequest));

        assertEquals("Card suit is required", exception.getMessage());
    }

    // Verifies missing move actions are rejected.
    @Test
    void shouldRejectMissingMoveAction() {
        PlayTurnTestContext context = launchPlayableGame();
        MoveRequestDto moveRequest = moveRequest(1, "HEARTS", " ", null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context.handler.handlePlayTurn(context.headerAccessor, context.roomId, moveRequest));

        assertEquals("Move action is required", exception.getMessage());
    }

    // Verifies invalid suits are rejected before reaching GameService.
    @Test
    void shouldRejectInvalidCardSuit() {
        PlayTurnTestContext context = launchPlayableGame();
        MoveRequestDto moveRequest = moveRequest(1, "NOT_A_SUIT", "CAPTURE_FROM_PILE", null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context.handler.handlePlayTurn(context.headerAccessor, context.roomId, moveRequest));

        assertEquals("Invalid card suit: NOT_A_SUIT", exception.getMessage());
    }

    // Verifies invalid actions are rejected before reaching GameService.
    @Test
    void shouldRejectInvalidMoveAction() {
        PlayTurnTestContext context = launchPlayableGame();
        MoveRequestDto moveRequest = moveRequest(1, "HEARTS", "NOT_A_MOVE", null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context.handler.handlePlayTurn(context.headerAccessor, context.roomId, moveRequest));

        assertEquals("Invalid move action: NOT_A_MOVE", exception.getMessage());
    }

    // Verifies missing game sessions are rejected.
    @Test
    void shouldRejectMissingGameSession() {
        PlayTurnTestContext context = launchPlayableGame();
        MoveRequestDto moveRequest = validPileMove(context);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> context.handler.handlePlayTurn(context.headerAccessor, "missing-room", moveRequest));

        assertEquals("No active game session for room: missing-room", exception.getMessage());
    }

    // Verifies missing session ids are rejected.
    @Test
    void shouldRejectMissingSessionId() {
        PlayTurnTestContext context = launchPlayableGame();
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context.handler.handlePlayTurn(headerAccessor, context.roomId, validPileMove(context)));

        assertEquals("Session ID cannot be null or empty", exception.getMessage());
    }

    // Verifies unknown sessions are rejected after lookup.
    @Test
    void shouldRejectUnknownPlayerForSession() {
        PlayTurnTestContext context = launchPlayableGame();
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession("unknown-session");

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> context.handler.handlePlayTurn(headerAccessor, context.roomId, validPileMove(context)));

        assertEquals("Player not found for session: unknown-session", exception.getMessage());
    }

    // Verifies opponent moves require a facing seat.
    @Test
    void shouldRejectOpponentMoveWithoutFacingSeat() {
        PlayTurnTestContext context = launchPlayableGame();
        MoveRequestDto moveRequest = moveRequest(1, "HEARTS", "CAPTURE_FROM_OPPONENT", null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context.handler.handlePlayTurn(context.headerAccessor, context.roomId, moveRequest));

        assertEquals("Facing seat is required for CAPTURE_FROM_OPPONENT", exception.getMessage());
    }

    // Verifies opponent moves reject seats that do not exist.
    @Test
    void shouldRejectOpponentMoveWithMissingFacingPlayer() {
        PlayTurnTestContext context = launchPlayableGame();
        MoveRequestDto moveRequest = moveRequest(1, "HEARTS", "CAPTURE_FROM_OPPONENT", 9);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> context.handler.handlePlayTurn(context.headerAccessor, context.roomId, moveRequest));

        assertEquals("Facing player not found for seat: 9", exception.getMessage());
    }

    private PlayTurnTestContext launchPlayableGame() {
        RoomService roomService = new RoomService();
        GameRegistry gameRegistry = new GameRegistry();
        GameService setupGameService = new GameService(roomService, gameRegistry);
        String roomId = createSeatedRoom(roomService);
        setupGameService.launchGame(roomId, "LAUNCH_GAME");

        GameService gameService = mock(GameService.class);
        PlayTurnHandler handler = new PlayTurnHandler(gameService, gameRegistry);
        GameSession session = gameRegistry.get(roomId);
        SimpMessageHeaderAccessor headerAccessor = headerAccessorWithSession(session.getGameEngine().getCurrentPlayer().getSessionId());

        return new PlayTurnTestContext(roomId, gameRegistry, gameService, handler, headerAccessor, session, session.getGameEngine());
    }

    private String createSeatedRoom(RoomService roomService) {
        String roomId = roomService.createRoom("owner-session", "Alice");
        roomService.join(roomId, "owner-session", "Alice", 0);
        roomService.join(roomId, "session-2", "Bob", 1);
        roomService.join(roomId, "session-3", "Carol", 2);
        roomService.join(roomId, "session-4", "Dave", 3);
        return roomId;
    }

    private MoveRequestDto validPileMove(PlayTurnTestContext context) {
        Player currentPlayer = context.engine.getCurrentPlayer();
        Card chosenCard = currentPlayer.getHand().get(0);
        return moveRequest(chosenCard.getNumber(), chosenCard.getSuit().name(), "CAPTURE_FROM_PILE", null);
    }

    private MoveRequestDto moveRequest(Integer number, String suit, String action, Integer facingSeat) {
        MoveRequestDto moveRequest = new MoveRequestDto();
        MoveRequestDto.CardRequest cardRequest = new MoveRequestDto.CardRequest();
        cardRequest.setNumber(number);
        cardRequest.setSuit(suit);
        moveRequest.setCard(cardRequest);
        moveRequest.setAction(action);
        moveRequest.setFacingSeat(facingSeat);
        return moveRequest;
    }

    private SimpMessageHeaderAccessor headerAccessorWithSession(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        Map<String, Object> sessionAttributes = new HashMap<>();
        if (sessionId != null) {
            sessionAttributes.put(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME, sessionId);
        }
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
        return headerAccessor;
    }

    private static class PlayTurnTestContext {
        private final String roomId;
        private final GameRegistry gameRegistry;
        private final GameService gameService;
        private final PlayTurnHandler handler;
        private final SimpMessageHeaderAccessor headerAccessor;
        private final GameSession session;
        private final GameEngineV2 engine;

        private PlayTurnTestContext(
                String roomId,
                GameRegistry gameRegistry,
                GameService gameService,
                PlayTurnHandler handler,
                SimpMessageHeaderAccessor headerAccessor,
                GameSession session,
                GameEngineV2 engine) {
            this.roomId = roomId;
            this.gameRegistry = gameRegistry;
            this.gameService = gameService;
            this.handler = handler;
            this.headerAccessor = headerAccessor;
            this.session = session;
            this.engine = engine;
        }
    }
}