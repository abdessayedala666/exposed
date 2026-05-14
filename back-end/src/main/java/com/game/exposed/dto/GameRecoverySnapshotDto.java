package com.game.exposed.dto;

import java.util.List;
import java.util.Map;

import com.game.exposed.game.Components.Card;

public class GameRecoverySnapshotDto {

    private String roomId;
    private String gameStatus;
    private Integer roundNumber;
    private String roundStatus;
    private Integer currentPlayerIndex;
    private Integer currentDealerIndex;
    private Integer setNumber;
    private String setStatus;
    private Integer roundsCompleted;
    private String winnerTeam;
    private Integer team1Score;
    private Integer team2Score;

    private PlayerState roomOwner;
    private Map<Integer, PlayerState> seats;

    private TeamState team1;
    private TeamState team2;

    private List<Card> deck;
    private List<Card> tablePile;

    private List<SetResultState> setResults;

    private Integer snapshotVersion;
    private Long updatedAtEpochMs;
    private Integer schemaVersion;

    public GameRecoverySnapshotDto() {
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getRoundStatus() {
        return roundStatus;
    }

    public void setRoundStatus(String roundStatus) {
        this.roundStatus = roundStatus;
    }

    public Integer getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(Integer currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public Integer getCurrentDealerIndex() {
        return currentDealerIndex;
    }

    public void setCurrentDealerIndex(Integer currentDealerIndex) {
        this.currentDealerIndex = currentDealerIndex;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    public String getSetStatus() {
        return setStatus;
    }

    public void setSetStatus(String setStatus) {
        this.setStatus = setStatus;
    }

    public Integer getRoundsCompleted() {
        return roundsCompleted;
    }

    public void setRoundsCompleted(Integer roundsCompleted) {
        this.roundsCompleted = roundsCompleted;
    }

    public String getWinnerTeam() {
        return winnerTeam;
    }

    public void setWinnerTeam(String winnerTeam) {
        this.winnerTeam = winnerTeam;
    }

    public Integer getTeam1Score() {
        return team1Score;
    }

    public void setTeam1Score(Integer team1Score) {
        this.team1Score = team1Score;
    }

    public Integer getTeam2Score() {
        return team2Score;
    }

    public void setTeam2Score(Integer team2Score) {
        this.team2Score = team2Score;
    }

    public PlayerState getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(PlayerState roomOwner) {
        this.roomOwner = roomOwner;
    }

    public Map<Integer, PlayerState> getSeats() {
        return seats;
    }

    public void setSeats(Map<Integer, PlayerState> seats) {
        this.seats = seats;
    }

    public TeamState getTeam1() {
        return team1;
    }

    public void setTeam1(TeamState team1) {
        this.team1 = team1;
    }

    public TeamState getTeam2() {
        return team2;
    }

    public void setTeam2(TeamState team2) {
        this.team2 = team2;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public void setDeck(List<Card> deck) {
        this.deck = deck;
    }

    public List<Card> getTablePile() {
        return tablePile;
    }

    public void setTablePile(List<Card> tablePile) {
        this.tablePile = tablePile;
    }

    public List<SetResultState> getSetResults() {
        return setResults;
    }

    public void setSetResults(List<SetResultState> setResults) {
        this.setResults = setResults;
    }

    public Integer getSnapshotVersion() {
        return snapshotVersion;
    }

    public void setSnapshotVersion(Integer snapshotVersion) {
        this.snapshotVersion = snapshotVersion;
    }

    public Long getUpdatedAtEpochMs() {
        return updatedAtEpochMs;
    }

    public void setUpdatedAtEpochMs(Long updatedAtEpochMs) {
        this.updatedAtEpochMs = updatedAtEpochMs;
    }

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public static class PlayerState {
        private Integer index;
        private String sessionId;
        private String name;
        private List<Card> hand;
        private List<Card> capturedCards;

        public PlayerState() {
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Card> getHand() {
            return hand;
        }

        public void setHand(List<Card> hand) {
            this.hand = hand;
        }

        public List<Card> getCapturedCards() {
            return capturedCards;
        }

        public void setCapturedCards(List<Card> capturedCards) {
            this.capturedCards = capturedCards;
        }
    }

    public static class TeamState {
        private List<PlayerState> players;
        private Integer totalScore;

        public TeamState() {
        }

        public List<PlayerState> getPlayers() {
            return players;
        }

        public void setPlayers(List<PlayerState> players) {
            this.players = players;
        }

        public Integer getTotalScore() {
            return totalScore;
        }

        public void setTotalScore(Integer totalScore) {
            this.totalScore = totalScore;
        }
    }

    public static class SetResultState {
        private Integer setNumber;
        private Integer team1SetDelta;
        private Integer team2SetDelta;
        private Integer team1Total;
        private Integer team2Total;

        public SetResultState() {
        }

        public Integer getSetNumber() {
            return setNumber;
        }

        public void setSetNumber(Integer setNumber) {
            this.setNumber = setNumber;
        }

        public Integer getTeam1SetDelta() {
            return team1SetDelta;
        }

        public void setTeam1SetDelta(Integer team1SetDelta) {
            this.team1SetDelta = team1SetDelta;
        }

        public Integer getTeam2SetDelta() {
            return team2SetDelta;
        }

        public void setTeam2SetDelta(Integer team2SetDelta) {
            this.team2SetDelta = team2SetDelta;
        }

        public Integer getTeam1Total() {
            return team1Total;
        }

        public void setTeam1Total(Integer team1Total) {
            this.team1Total = team1Total;
        }

        public Integer getTeam2Total() {
            return team2Total;
        }

        public void setTeam2Total(Integer team2Total) {
            this.team2Total = team2Total;
        }
    }
}
