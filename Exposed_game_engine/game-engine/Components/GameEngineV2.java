import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * GameEngineV2 is the single orchestrator for match flow:
 * set lifecycle, round lifecycle, turn validation, move execution,
 * scoring, winner detection, and transition to next set.
 */
public class GameEngineV2 {
    // Match ends when a team reaches this threshold and wins tie-break rules.
    private static final int WINNING_SCORE = 310;

    // Team references are stable across the whole match.
    private final List<Team> teams = new ArrayList<>();
    // Match-level state container (players order, status, history).
    private final GameState gameState;
    // Active set state (deck, set status, rounds completed).
    private SetState setState;
    // Active round state (turn pointer, table pile, round status).
    private RoundState roundState;
    // Filled when match is finished.
    private Team winnerTeam;

    /**
     * Creates an engine for exactly 2 teams.
     */
    public GameEngineV2(Team team1, Team team2) {
        if (team1 == null || team2 == null) {
            throw new IllegalArgumentException("teams cannot be null");
        }
        teams.add(team1);
        teams.add(team2);
        // Dealer starts at seat 0 for first set.
        this.gameState = new GameState(teams, 0);
    }

    /**
     * Entry point to start the match.
     */
    public void startGame() {
        if (isGameOver()) {
            throw new IllegalStateException("game is already finished");
        }
        if (setState != null && !isSetOver()) {
            throw new IllegalStateException("a set is already active");
        }
        gameState.setGameStatus("IN_PROGRESS");
        startSet();
        startRound(1);
    }

    /**
     * Starts a fresh set with a fresh deck.
     */
    public void startSet() {
        if (setState != null && !isSetOver()) {
            throw new IllegalStateException("cannot start a new set while current set is active");
        }
        setState = new SetState();
        gameState.setCurrentSet(setState);
    }

    /**
     * Starts a specific round number inside the active set.
     */
    public void startRound(int roundNumber) {
        if (setState == null) {
            throw new IllegalStateException("cannot start round without an active set");
        }
        if (roundNumber < 1 || roundNumber > 3) {
            throw new IllegalArgumentException("roundNumber must be between 1 and 3");
        }

        // RoundState is recreated each round, so we explicitly carry table pile forward
        // for rounds 2 and 3. The pile should reset only when a new set starts.
        List<Card> previousPile = this.roundState == null
                ? Collections.emptyList()
                : new ArrayList<>(this.roundState.getTablePile());

        // New round object for clean round-scoped state.
        this.roundState = new RoundState(roundNumber);
        this.roundState.setRoundStatus("ACTIVE");
        this.gameState.setCurrentRound(this.roundState);
        this.setState.setRoundsCompleted(roundNumber);

        // First player to act is the seat after dealer.
        int playerCount = gameState.getPlayers().size();
        int dealerIndex = gameState.getCurrentDealerIndex();
        roundState.setCurrentPlayerIndex((dealerIndex + 1) % playerCount);

        // Deal 3 cards to each player every round.
        distributeCards();

        // Round 1 starts with 4 table cards from deck; later rounds keep previous pile.
        if (roundNumber == 1) {
            roundState.setTablePile(setState.getDeck().dealCards(4));
        } else {
            roundState.setTablePile(previousPile);
        }
    }

    /**
     * Executes one player turn and returns a move summary for UI/client.
     */
    public MoveResult playTurn(Player player, int chosenCardNumber, CaptureType captureType, Player facingPlayer) {
        // Guard: round must be active.
        if (roundState == null || !"ACTIVE".equals(roundState.getRoundStatus())) {
            throw new IllegalStateException("round is not active");
        }
        // Guard: game must not be finished.
        if (isGameOver()) {
            throw new IllegalStateException("game is already finished");
        }
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(captureType, "captureType cannot be null");

        // Guard: enforce strict turn ownership.
        Player currentPlayer = getCurrentPlayer();
        if (currentPlayer != player) {
            throw new IllegalArgumentException("it is not this player's turn");
        }

        // Remove chosen card from player's hand.
        Card cardPlayed = player.playCard(chosenCardNumber);
        List<Card> capturedThisTurn = new ArrayList<>();

        if (captureType == CaptureType.CAPTURE_FROM_PILE) {
            // Capture from pile only if top pile card number matches played card.
            if (!roundState.getTablePile().isEmpty()
                    && roundState.getTopTableCard().getNumber() == cardPlayed.getNumber()) {
                capturedThisTurn.add(cardPlayed);
                capturedThisTurn.add(roundState.removeTopTableCard());
                player.collectCards(capturedThisTurn);
            } else {
                // No match -> played card stays on table pile.
                roundState.addToTablePile(cardPlayed);
            }
        } else {
            // Capture from opponent requires an explicit target.
            if (facingPlayer == null) {
                throw new IllegalArgumentException("facingPlayer is required for CAPTURE_FROM_OPPONENT");
            }
            if (facingPlayer.getCapturedCards().isEmpty()) {
                throw new IllegalStateException("facingPlayer has no captured stack to steal");
            }
            if (facingPlayer.getTopcapturedCard().getNumber() != cardPlayed.getNumber()) {
                throw new IllegalArgumentException("played card number must match opponent top captured stack number");
            }

            // Steal full top stack of same number and add played card into that stack.
            capturedThisTurn.addAll(facingPlayer.removeTopCapturedStack());
            capturedThisTurn.add(cardPlayed);
            player.collectCards(capturedThisTurn);
        }

        // Move turn pointer to the next seat.
        advanceTurn();

        // Auto-close round when everyone exhausted their hand.
        boolean roundEnded = allHandsEmpty();
        boolean setEnded = false;
        boolean gameEnded = false;

        if (roundEnded) {
            EndSetOutcome outcome = endRoundInternal();
            setEnded = outcome.setEnded;
            gameEnded = outcome.gameEnded;
        }

        // Return rich move payload for UI/logging.
        return new MoveResult(
                player,
                cardPlayed,
                captureType,
                capturedThisTurn,
                getCurrentPlayerIndex(),
                roundEnded,
                setEnded,
                gameEnded);
    }

    /** Returns the active player for the current turn. */
    public Player getCurrentPlayer() {
        if (roundState == null) {
            throw new IllegalStateException("round has not started");
        }
        return gameState.getPlayers().get(roundState.getCurrentPlayerIndex());
    }

    /** Returns active player index, or -1 before first round starts. */
    public int getCurrentPlayerIndex() {
        if (roundState == null) {
            return -1;
        }
        return roundState.getCurrentPlayerIndex();
    }

    /** True when round lifecycle reached FINISHED status. */
    public boolean isRoundOver() {
        return roundState != null && "FINISHED".equals(roundState.getRoundStatus());
    }

    /** True when set lifecycle reached FINISHED status. */
    public boolean isSetOver() {
        return setState != null && "FINISHED".equals(setState.getSetStatus());
    }

    /** True when match status reached FINISHED. */
    public boolean isGameOver() {
        return "FINISHED".equals(gameState.getGameStatus());
    }

    /** Winner team if game has ended, otherwise null. */
    public Team getWinnerTeamOrNull() {
        return winnerTeam;
    }

    /** Exposes match snapshot for UI/diagnostics. */
    public GameState getGameState() {
        return gameState;
    }

    /** Deals 3 cards per player in dealer-rotated order. */
    private void distributeCards() {
        int dealerIndex = gameState.getCurrentDealerIndex();
        int playerCount = gameState.getPlayers().size();

        for (int i = 0; i < playerCount; i++) {
            List<Card> drawnCards = setState.getDeck().dealCards(3);
            gameState.getPlayers().get((dealerIndex + i) % playerCount).setHand(drawnCards);
        }
    }

    /** Advances current turn pointer clockwise. */
    private void advanceTurn() {
        int playerCount = gameState.getPlayers().size();
        int nextIndex = (roundState.getCurrentPlayerIndex() + 1) % playerCount;
        roundState.setCurrentPlayerIndex(nextIndex);
    }

    /** Utility: true when all players are out of hand cards for this round. */
    private boolean allHandsEmpty() {
        return gameState.getPlayers().stream().allMatch(player -> player.getHand().isEmpty());
    }

    /**
     * Finalizes round and transitions to next round or set.
     */
    private EndSetOutcome endRoundInternal() {
        if (!allHandsEmpty()) {
            throw new IllegalStateException("cannot end round while some players still have cards");
        }

        int roundNumber = roundState.getRoundNumber();
        roundState.setRoundStatus("FINISHED");
        setState.setRoundsCompleted(roundNumber);

        // Hands are round-scoped, so clear now.
        gameState.getPlayers().forEach(Player::clearHand);

        // Rounds 1 and 2 transition into another round.
        if (roundNumber < 3) {
            startRound(roundNumber + 1);
            return new EndSetOutcome(false, false);
        }

        // Round 3 transitions into set end.
        return endSetInternal();
    }

    /**
     * Finalizes set: scoring, reset, winner check, and transition.
     */
    private EndSetOutcome endSetInternal() {
        // Capture set deltas before updating cumulative totals.
        int teamOneSetDelta = teams.get(0).calculateSetScore();
        int teamTwoSetDelta = teams.get(1).calculateSetScore();

        // Persist set score into long-term totals.
        teams.get(0).CalculateTotalScore();
        teams.get(1).CalculateTotalScore();

        // Keep set-by-set history for UI/audit.
        gameState.addSetResult(new GameState.SetResult(
                gameState.getSetResults().size() + 1,
                teamOneSetDelta,
                teamTwoSetDelta,
                teams.get(0).getTotalScore(),
                teams.get(1).getTotalScore()));

        // Captured stacks are set-scoped; reset them for next set.
        gameState.getPlayers().forEach(player -> {
            player.clearHand();
            player.clearCapturedCards();
        });

        setState.setRoundsCompleted(3);
        setState.setSetStatus("FINISHED");

        int teamOneTotal = teams.get(0).getTotalScore();
        int teamTwoTotal = teams.get(1).getTotalScore();

        Team winner = null;
        // Winner check: threshold plus higher total tie-break.
        if (teams.get(0).hasWon(WINNING_SCORE) || teams.get(1).hasWon(WINNING_SCORE)) {
            if (teamOneTotal > teamTwoTotal) {
                winner = teams.get(0);
            } else if (teamTwoTotal > teamOneTotal) {
                winner = teams.get(1);
            }
        }

        if (winner != null) {
            setState.setSetWinner(winner);
            winnerTeam = winner;
            gameState.setGameStatus("FINISHED");
            return new EndSetOutcome(true, true);
        }

        // No winner yet: rotate dealer and continue with a fresh set.
        gameState.advanceDealerIndex();
        startSet();
        startRound(1);
        return new EndSetOutcome(true, false);
    }

    /** Internal transition result used by playTurn auto-branching. */
    private static class EndSetOutcome {
        private final boolean setEnded;
        private final boolean gameEnded;

        private EndSetOutcome(boolean setEnded, boolean gameEnded) {
            this.setEnded = setEnded;
            this.gameEnded = gameEnded;
        }
    }

    /**
     * Immutable move payload returned to UI/clients after each turn.
     */
    public static class MoveResult {
        // Player who performed the move.
        private final Player actor;
        // Card removed from hand and played this turn.
        private final Card playedCard;
        // How move was resolved (pile or opponent capture).
        private final CaptureType captureType;
        // Cards captured by this move (empty if no capture).
        private final List<Card> capturedCards;
        // Turn index after move resolution and automatic transitions.
        private final int nextPlayerIndex;
        // Lifecycle flags for external UI flow.
        private final boolean roundEnded;
        private final boolean setEnded;
        private final boolean gameEnded;

        public MoveResult(
                Player actor,
                Card playedCard,
                CaptureType captureType,
                List<Card> capturedCards,
                int nextPlayerIndex,
                boolean roundEnded,
                boolean setEnded,
                boolean gameEnded) {
            this.actor = actor;
            this.playedCard = playedCard;
            this.captureType = captureType;
            // Defensive copy keeps result immutable from outside.
            this.capturedCards = new ArrayList<>(capturedCards);
            this.nextPlayerIndex = nextPlayerIndex;
            this.roundEnded = roundEnded;
            this.setEnded = setEnded;
            this.gameEnded = gameEnded;
        }

        /** Actor who played the move. */
        public Player getActor() {
            return actor;
        }

        /** Card played from hand. */
        public Card getPlayedCard() {
            return playedCard;
        }

        /** Move resolution type. */
        public CaptureType getCaptureType() {
            return captureType;
        }

        /** Captured cards for this move (read-only). */
        public List<Card> getCapturedCards() {
            return Collections.unmodifiableList(capturedCards);
        }

        /** Next current player index. */
        public int getNextPlayerIndex() {
            return nextPlayerIndex;
        }

        /** True if this move ended the current round. */
        public boolean isRoundEnded() {
            return roundEnded;
        }

        /** True if this move ended the current set. */
        public boolean isSetEnded() {
            return setEnded;
        }

        /** True if this move ended the whole match. */
        public boolean isGameEnded() {
            return gameEnded;
        }
    }
}
