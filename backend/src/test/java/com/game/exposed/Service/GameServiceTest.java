package com.game.exposed.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.game.exposed.Exceptions.InvalidOperationException;
import com.game.exposed.dto.MoveResult;
import com.game.exposed.dto.Room;
import com.game.exposed.game.Components.CaptureType;
import com.game.exposed.game.Components.Card;
import com.game.exposed.game.Components.GameEngineV2;
import com.game.exposed.game.Components.Player;
import com.game.exposed.game.Components.Team;

class GameServiceTest {

    private GameTestContext context;

    @BeforeEach
    void setUp() {
        context = launchStandardGame();
    }

    // Verifies launch creates a stored session for the room.
    @Test
    void launchGameShouldSaveGameSessionInRegistry() {
        assertTrue(context.gameRegistry.exists(context.roomId));
        assertSame(context.session, context.gameRegistry.get(context.roomId));
    }

    // Verifies launch returns snapshots for all four players.
    @Test
    void launchGameShouldReturnFourSnapshots() {
        assertEquals(4, context.launchSnapshots.size());
    }

    // Verifies launch keys snapshots by websocket user id.
    @Test
    void launchGameShouldKeySnapshotsBySessionIdAndName() {
        assertTrue(context.launchSnapshots.containsKey("owner-session:Alice"));
        assertTrue(context.launchSnapshots.containsKey("session-2:Bob"));
        assertTrue(context.launchSnapshots.containsKey("session-3:Carol"));
        assertTrue(context.launchSnapshots.containsKey("session-4:Dave"));
    }

    // Verifies launch starts the match in progress.
    @Test
    void launchGameShouldStartGameInProgress() {
        assertEquals("IN_PROGRESS", context.engine.getGameState().getGameStatus());
    }

    // Verifies launch sets the current player in the returned snapshots.
    @Test
    void launchGameShouldExposeCurrentPlayerName() {
        MoveResult bobSnapshot = context.launchSnapshots.get("session-2:Bob");

        assertEquals("Bob", bobSnapshot.getCurrentPlayerName());
    }

    // Verifies launch deals three cards to each player.
    @Test
    void launchGameShouldDealThreeCardsPerPlayer() {
        context.engine.getGameState().getPlayers().forEach(player -> assertEquals(3, player.getHand().size()));
    }

    // Verifies launch exposes the first round number.
    @Test
    void launchGameShouldSetRoundNumberToOne() {
        context.launchSnapshots.values().forEach(snapshot -> assertEquals(1, snapshot.getRoundNumber()));
    }

    // Verifies launch exposes the first set number.
    @Test
    void launchGameShouldSetSetNumberToOne() {
        context.launchSnapshots.values().forEach(snapshot -> assertEquals(1, snapshot.getSetNumber()));
    }

    // Verifies launch marks the round as active.
    @Test
    void launchGameShouldMarkRoundAsActive() {
        context.launchSnapshots.values().forEach(snapshot -> assertEquals("ACTIVE", snapshot.getRoundState()));
    }

    // Verifies launch marks the set as active.
    @Test
    void launchGameShouldMarkSetAsActive() {
        context.launchSnapshots.values().forEach(snapshot -> assertEquals("ACTIVE", snapshot.getSetState()));
    }

    // Verifies launch stores both team names in the snapshot.
    @Test
    void launchGameShouldExposeTeamDisplayNames() {
        MoveResult snapshot = context.launchSnapshots.get("owner-session:Alice");

        assertEquals(List.of("Alice", "Carol"), snapshot.getTeamDisplayNames().get("team1"));
        assertEquals(List.of("Bob", "Dave"), snapshot.getTeamDisplayNames().get("team2"));
    }

    // Verifies launch initializes team scores at zero.
    @Test
    void launchGameShouldExposeZeroTeamScores() {
        context.launchSnapshots.values().forEach(snapshot -> {
            assertEquals(0, snapshot.getTeamScores().get("team1"));
            assertEquals(0, snapshot.getTeamScores().get("team2"));
        });
    }

    // Verifies launch exposes each player's seat index correctly.
    @Test
    void launchGameShouldExposeSeatIndexes() {
        assertEquals(0, context.launchSnapshots.get("owner-session:Alice").getSeat());
        assertEquals(1, context.launchSnapshots.get("session-2:Bob").getSeat());
        assertEquals(2, context.launchSnapshots.get("session-3:Carol").getSeat());
        assertEquals(3, context.launchSnapshots.get("session-4:Dave").getSeat());
    }

    // Verifies launch includes the top card on the table.
    @Test
    void launchGameShouldExposeTopTableCard() {
        context.launchSnapshots.values().forEach(snapshot -> assertNotNull(snapshot.getTopCardInPile()));
    }

    // Verifies launch includes captured-card tracking for every seat.
    @Test
    void launchGameShouldExposeCapturedCardsBySeat() {
        context.launchSnapshots.values().forEach(snapshot -> {
            assertEquals(4, snapshot.getCapturedCardsByPlayerIndex().size());
            assertTrue(snapshot.getCapturedCardsByPlayerIndex().values().stream().allMatch(List::isEmpty));
        });
    }

    // Verifies launch includes hand-size tracking for every seat.
    @Test
    void launchGameShouldExposeCardCountsBySeat() {
        context.launchSnapshots.values().forEach(snapshot -> {
            assertEquals(4, snapshot.getCardsNumberByPlayerIndex().size());
            assertEquals(3, snapshot.getCardsNumberByPlayerIndex().get(0));
            assertEquals(3, snapshot.getCardsNumberByPlayerIndex().get(1));
            assertEquals(3, snapshot.getCardsNumberByPlayerIndex().get(2));
            assertEquals(3, snapshot.getCardsNumberByPlayerIndex().get(3));
        });
    }

    // Verifies blank room ids are rejected for current-state lookup.
    @Test
    void getCurrentStateForPlayerShouldRejectBlankRoomId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context.gameService.getCurrentStateForPlayer(" ", "Bob"));

        assertEquals("Room ID cannot be null or empty", exception.getMessage());
    }

    // Verifies blank player names are rejected for current-state lookup.
    @Test
    void getCurrentStateForPlayerShouldRejectBlankPlayerName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> context.gameService.getCurrentStateForPlayer(context.roomId, " "));

        assertEquals("Player name cannot be null or empty", exception.getMessage());
    }

    // Verifies current-state lookup returns null when there is no game session.
    @Test
    void getCurrentStateForPlayerShouldReturnNullWhenSessionIsMissing() {
        RoomService roomService = new RoomService();
        GameRegistry gameRegistry = new GameRegistry();
        GameService gameService = new GameService(roomService, gameRegistry);
        String roomId = createSeatedRoom(roomService);

        assertNull(gameService.getCurrentStateForPlayer(roomId, "Bob"));
    }

    // Verifies current-state lookup returns null when the session has no engine.
    @Test
    void getCurrentStateForPlayerShouldReturnNullWhenEngineIsMissing() {
        RoomService roomService = new RoomService();
        GameRegistry gameRegistry = new GameRegistry();
        GameService gameService = new GameService(roomService, gameRegistry);
        String roomId = createSeatedRoom(roomService);
        gameRegistry.save(roomId, new GameSession(roomId, null));

        assertNull(gameService.getCurrentStateForPlayer(roomId, "Bob"));
    }

    // Verifies current-state lookup returns null for a player not in the match.
    @Test
    void getCurrentStateForPlayerShouldReturnNullForUnknownPlayer() {
        assertNull(context.gameService.getCurrentStateForPlayer(context.roomId, "Nobody"));
    }

    // Verifies current-state lookup returns the active player's snapshot.
    @Test
    void getCurrentStateForPlayerShouldReturnSnapshotForCurrentPlayer() {
        MoveResult snapshot = context.gameService.getCurrentStateForPlayer(context.roomId, "Bob");

        assertNotNull(snapshot);
        assertEquals("INITIAL_HAND", snapshot.getType());
        assertEquals("Bob", snapshot.getPlayerName());
        assertEquals(1, snapshot.getSeat());
        assertEquals("Bob", snapshot.getCurrentPlayerName());
        assertEquals(3, snapshot.getHand().size());
    }

    // Verifies current-state lookup preserves the room id.
    @Test
    void getCurrentStateForPlayerShouldExposeRoomId() {
        MoveResult snapshot = context.gameService.getCurrentStateForPlayer(context.roomId, "Bob");

        assertEquals(context.roomId, snapshot.getRoomId());
    }

    // Verifies current-state lookup sees the same top table card as launch.
    @Test
    void getCurrentStateForPlayerShouldExposeSameTopCardAsLaunch() {
        MoveResult launchSnapshot = context.launchSnapshots.get("session-2:Bob");
        MoveResult currentStateSnapshot = context.gameService.getCurrentStateForPlayer(context.roomId, "Bob");

        assertEquals(launchSnapshot.getTopCardInPile(), currentStateSnapshot.getTopCardInPile());
    }

    // Verifies current-state lookup preserves team names.
    @Test
    void getCurrentStateForPlayerShouldExposeTeamNames() {
        MoveResult snapshot = context.gameService.getCurrentStateForPlayer(context.roomId, "Bob");

        assertEquals(List.of("Alice", "Carol"), snapshot.getTeamDisplayNames().get("team1"));
        assertEquals(List.of("Bob", "Dave"), snapshot.getTeamDisplayNames().get("team2"));
    }

    // Verifies current-state lookup preserves team scores before any move.
    @Test
    void getCurrentStateForPlayerShouldExposeZeroTeamScores() {
        MoveResult snapshot = context.gameService.getCurrentStateForPlayer(context.roomId, "Bob");

        assertEquals(0, snapshot.getTeamScores().get("team1"));
        assertEquals(0, snapshot.getTeamScores().get("team2"));
    }

    // Verifies current-state lookup includes round and set metadata.
    @Test
    void getCurrentStateForPlayerShouldExposeRoundAndSetInfo() {
        MoveResult snapshot = context.gameService.getCurrentStateForPlayer(context.roomId, "Bob");

        assertEquals(1, snapshot.getRoundNumber());
        assertEquals(1, snapshot.getSetNumber());
        assertEquals("ACTIVE", snapshot.getRoundState());
        assertEquals("ACTIVE", snapshot.getSetState());
        assertEquals("IN_PROGRESS", snapshot.getGameState());
        assertNull(snapshot.getWinnerTeam());
    }

    // Verifies current-state lookup carries per-seat card counts.
    @Test
    void getCurrentStateForPlayerShouldExposeCardCountsBySeat() {
        MoveResult snapshot = context.gameService.getCurrentStateForPlayer(context.roomId, "Bob");

        assertEquals(3, snapshot.getCardsNumberByPlayerIndex().get(0));
        assertEquals(3, snapshot.getCardsNumberByPlayerIndex().get(1));
        assertEquals(3, snapshot.getCardsNumberByPlayerIndex().get(2));
        assertEquals(3, snapshot.getCardsNumberByPlayerIndex().get(3));
    }

    // Verifies one turn produces move snapshots for all four players.
    @Test
    void playTurnShouldReturnSnapshotsForAllPlayers() {
        Map<String, MoveResult> result = playSingleTurn(context);

        assertEquals(4, result.size());
    }

    // Verifies the acting player's hand shrinks by one after a turn.
    @Test
    void playTurnShouldDecreaseActorsHandByOne() {
        Player currentPlayer = context.engine.getCurrentPlayer();
        int before = currentPlayer.getHand().size();

        Map<String, MoveResult> result = playSingleTurn(context);

        MoveResult actorSnapshot = result.get(wsUser(currentPlayer));
        assertEquals(before - 1, actorSnapshot.getHand().size());
    }

    // Verifies the returned snapshots point to the next player.
    @Test
    void playTurnShouldAdvanceCurrentPlayer() {
        Map<String, MoveResult> result = playSingleTurn(context);

        assertEquals("Carol", result.values().iterator().next().getCurrentPlayerName());
    }

    // Verifies the round remains active after a single legal move.
    @Test
    void playTurnShouldKeepRoundActiveAfterFirstMove() {
        Map<String, MoveResult> result = playSingleTurn(context);

        assertEquals("ACTIVE", result.values().iterator().next().getRoundState());
        assertEquals("ACTIVE", context.engine.getGameState().getCurrentRound().getRoundStatus());
    }

    // Verifies the move snapshot updates per-seat card counts.
    @Test
    void playTurnShouldUpdateCardCountsBySeat() {
        Player currentPlayer = context.engine.getCurrentPlayer();

        Map<String, MoveResult> result = playSingleTurn(context);
        MoveResult actorSnapshot = result.get(wsUser(currentPlayer));

        assertEquals(2, actorSnapshot.getCardsNumberByPlayerIndex().get(currentPlayer.getIndex()));
        result.values().forEach(snapshot -> assertEquals(4, snapshot.getCardsNumberByPlayerIndex().size()));
    }

    // Verifies the move snapshot still carries team state after a turn.
    @Test
    void playTurnShouldPreserveTeamStateAfterMove() {
        Map<String, MoveResult> result = playSingleTurn(context);
        MoveResult actorSnapshot = result.get("session-2:Bob");

        assertEquals(List.of("Alice", "Carol"), actorSnapshot.getTeamDisplayNames().get("team1"));
        assertEquals(List.of("Bob", "Dave"), actorSnapshot.getTeamDisplayNames().get("team2"));
    }

    // Verifies a non-current player cannot play out of turn.
    @Test
    void playTurnShouldRejectNonCurrentPlayer() {
        Player wrongPlayer = context.engine.getGameState().getPlayers().get(0);
        Card chosenCard = wrongPlayer.getHand().get(0);

        assertThrows(IllegalArgumentException.class, () -> context.gameService.playTurn(
                context.roomId,
                context.session,
                wrongPlayer,
                null,
                CaptureType.CAPTURE_FROM_PILE,
                chosenCard));
    }

    // Verifies a boosted match can run all the way to completion.
    @Test
    void fullGameShouldCompleteWithoutErrors() throws Exception {
        boostTeamScore(context.engine, 0, 1000);
        boostTeamScore(context.engine, 1, 0);

        playUntilGameEnds(context);

        assertTrue(context.engine.isGameOver());
        assertEquals("FINISHED", context.engine.getGameState().getGameStatus());
    }

    // Verifies the winner survives into the final snapshot after game end.
    @Test
    void fullGameShouldExposeWinnerTeamInSnapshot() throws Exception {
        boostTeamScore(context.engine, 0, 1000);
        boostTeamScore(context.engine, 1, 0);

        playUntilGameEnds(context);

        MoveResult snapshot = context.gameService.getCurrentStateForPlayer(context.roomId, "Alice");

        assertNotNull(snapshot);
        assertEquals("team1", snapshot.getWinnerTeam());
        assertEquals("FINISHED", snapshot.getGameState());
    }

    private GameTestContext launchStandardGame() {
        RoomService roomService = new RoomService();
        GameRegistry gameRegistry = new GameRegistry();
        GameService gameService = new GameService(roomService, gameRegistry);
        String roomId = createSeatedRoom(roomService);

        Map<String, MoveResult> launchSnapshots = gameService.launchGame(roomId, "LAUNCH_GAME");
        GameSession session = gameRegistry.get(roomId);

        return new GameTestContext(roomService, gameRegistry, gameService, roomId, launchSnapshots, session,
                session.getGameEngine());
    }

    private String createSeatedRoom(RoomService roomService) {
        String roomId = roomService.createRoom("owner-session", "Alice");
        roomService.join(roomId, "owner-session", "Alice", 0);
        roomService.join(roomId, "session-2", "Bob", 1);
        roomService.join(roomId, "session-3", "Carol", 2);
        roomService.join(roomId, "session-4", "Dave", 3);
        return roomId;
    }

    private Map<String, MoveResult> playSingleTurn(GameTestContext context) {
        Player currentPlayer = context.engine.getCurrentPlayer();
        Card chosenCard = currentPlayer.getHand().get(0);
        return context.gameService.playTurn(
                context.roomId,
                context.session,
                currentPlayer,
                null,
                CaptureType.CAPTURE_FROM_PILE,
                chosenCard);
    }

    private void playUntilGameEnds(GameTestContext context) {
        int safetyLimit = 60;
        int turns = 0;
        while (!context.engine.isGameOver() && turns < safetyLimit) {
            Player currentPlayer = context.engine.getCurrentPlayer();
            Card chosenCard = currentPlayer.getHand().get(0);
            context.gameService.playTurn(
                    context.roomId,
                    context.session,
                    currentPlayer,
                    null,
                    CaptureType.CAPTURE_FROM_PILE,
                    chosenCard);
            turns++;
        }
        assertTrue(turns < safetyLimit, "game did not finish within the safety limit");
    }

    private void boostTeamScore(GameEngineV2 engine, int teamIndex, int totalScore) throws Exception {
        Team team = engine.getGameState().getTeams().get(teamIndex);
        Field field = Team.class.getDeclaredField("totalScore");
        field.setAccessible(true);
        field.setInt(team, totalScore);
    }

    private String wsUser(Player player) {
        return player.getSessionId() + ":" + player.getName();
    }

    private static class GameTestContext {
        private final RoomService roomService;
        private final GameRegistry gameRegistry;
        private final GameService gameService;
        private final String roomId;
        private final Map<String, MoveResult> launchSnapshots;
        private final GameSession session;
        private final GameEngineV2 engine;

        private GameTestContext(
                RoomService roomService,
                GameRegistry gameRegistry,
                GameService gameService,
                String roomId,
                Map<String, MoveResult> launchSnapshots,
                GameSession session,
                GameEngineV2 engine) {
            this.roomService = roomService;
            this.gameRegistry = gameRegistry;
            this.gameService = gameService;
            this.roomId = roomId;
            this.launchSnapshots = launchSnapshots;
            this.session = session;
            this.engine = engine;
        }
    }
}