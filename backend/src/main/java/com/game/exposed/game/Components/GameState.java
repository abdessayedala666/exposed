package com.game.exposed.game.Components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the overall match state.
 * Holds long-lived entities and references to active set/round states.
 */
public class GameState {
    private final List<Team> teams;
    private final List<Player> players = new ArrayList<>();
    private final List<SetResult> setResults = new ArrayList<>();
    private int currentDealerIndex  ;
    private SetState currentSet;
    private RoundState currentRound;
    private String gameStatus;

    public GameState(List<Team> teams , int currentDealerIndex ) {
        if (teams == null || teams.size() != 2) {
            throw new IllegalArgumentException("GameState needs exactly 2 teams");
        }

        this.teams = teams;

        // Keep seat order alternating by team: T1P1, T2P1, T1P2, T2P2.
        Team teamOne = teams.get(0);
        Team teamTwo = teams.get(1);
        this.players.add(teamOne.getPlayer1());
        this.players.add(teamTwo.getPlayer1());
        this.players.add(teamOne.getPlayer2());
        this.players.add(teamTwo.getPlayer2());
        this.currentDealerIndex = currentDealerIndex ;
        this.gameStatus = "IN_PROGRESS";
    }

    public List<Team> getTeams() { return Collections.unmodifiableList(teams); }
    public List<Player> getPlayers() { return Collections.unmodifiableList(players); }
    public List<SetResult> getSetResults() { return Collections.unmodifiableList(setResults); }
    public void addSetResult(SetResult result) { this.setResults.add(result); }
    public SetState getCurrentSet() { return currentSet; }
    public void setCurrentSet(SetState currentSet) { this.currentSet = currentSet; }
    public RoundState getCurrentRound() { return currentRound; }
    public void setCurrentRound(RoundState round) { this.currentRound = round; }
    public String getGameStatus() { return gameStatus; }
    public void setGameStatus(String status) { this.gameStatus = status; }
    public void setCurrentDealerIndex(int currentDealerIndex ){
        this.currentDealerIndex = currentDealerIndex;
    }
    public int getCurrentDealerIndex(){
        return this.currentDealerIndex ;
    }

    public void advanceDealerIndex() {
        // Dealer rotates one seat clockwise after a set, so we keep the next dealer here.
        this.currentDealerIndex = (this.currentDealerIndex + 1) % players.size();
    }

    public static class SetResult {
        private final int setNumber;
        private final int teamOneDelta;
        private final int teamTwoDelta;
        private final int teamOneTotalAfterSet;
        private final int teamTwoTotalAfterSet;

        public SetResult(
                int setNumber,
                int teamOneDelta,
                int teamTwoDelta,
                int teamOneTotalAfterSet,
                int teamTwoTotalAfterSet) {
            this.setNumber = setNumber;
            this.teamOneDelta = teamOneDelta;
            this.teamTwoDelta = teamTwoDelta;
            this.teamOneTotalAfterSet = teamOneTotalAfterSet;
            this.teamTwoTotalAfterSet = teamTwoTotalAfterSet;
        }

        public int getSetNumber() {
            return setNumber;
        }

        public int getTeamOneDelta() {
            return teamOneDelta;
        }

        public int getTeamTwoDelta() {
            return teamTwoDelta;
        }

        public int getTeamOneTotalAfterSet() {
            return teamOneTotalAfterSet;
        }

        public int getTeamTwoTotalAfterSet() {
            return teamTwoTotalAfterSet;
        }
    }
}