package com.game.exposed.dto;

import java.util.List;
import java.util.Map;

import com.game.exposed.game.Components.Card;

public class MoveResult {

    private String type;
    private String roomId;
    private Integer seat;
    private String playerName;
    private List<Card> hand;
    private Map<Integer, List<Card>> capturedCardsByPlayerIndex;
    private Map<Integer , Integer> cardsNumberByPlayerIndex ;
    private String currentPlayerName;
    private Card topCardInPile;
    private Integer roundNumber;
    private Integer setNumber;
    private String roundState;
    private String setState;
    private String gameState;
    private String winnerTeam;
    private Map<String, List<String>> teamDisplayNames;
    private Map<String, Integer> teamScores;

    public MoveResult() {
    }

    public MoveResult(
            String type,
            String roomId,
            Integer seat,
            String playerName,
            List<Card> hand,
            Map<Integer, List<Card>> capturedCardsByPlayerIndex,
            Map<Integer , Integer> cardsNumberByPlayerIndex ,
            String currentPlayerName,
            Card topCardInPile,
            Integer roundNumber,
            Integer setNumber,
            String roundState,
            String setState,
            String gameState,
            String winnerTeam,
            Map<String, List<String>> teamDisplayNames,
            Map<String, Integer> teamScores) {
        this.type = type;
        this.roomId = roomId;
        this.seat = seat;
        this.playerName = playerName;
        this.hand = hand;
        this.capturedCardsByPlayerIndex = capturedCardsByPlayerIndex;
        this.cardsNumberByPlayerIndex = cardsNumberByPlayerIndex ;
        this.currentPlayerName = currentPlayerName;
        this.topCardInPile = topCardInPile;
        this.roundNumber = roundNumber;
        this.setNumber = setNumber;
        this.roundState = roundState;
        this.setState = setState;
        this.gameState = gameState;
        this.winnerTeam = winnerTeam;
        this.teamDisplayNames = teamDisplayNames;
        this.teamScores = teamScores;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Integer getSeat() {
        return seat;
    }

    public void setSeat(Integer seat) {
        this.seat = seat;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public Map<Integer, List<Card>> getCapturedCardsByPlayerIndex() {
        return capturedCardsByPlayerIndex;
    }

    public void setCapturedCardsByPlayerIndex(Map<Integer, List<Card>> capturedCardsByPlayerIndex) {
        this.capturedCardsByPlayerIndex = capturedCardsByPlayerIndex;
    }
    public void setCardsNumberByPlayerIndex(Map<Integer, Integer> cardsNumberByPlayerIndex){
        this.cardsNumberByPlayerIndex = cardsNumberByPlayerIndex ;
    }
    public Map<Integer, Integer> getCardsNumberByPlayerIndex(){
        return this.cardsNumberByPlayerIndex ;
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public void setCurrentPlayerName(String currentPlayerName) {
        this.currentPlayerName = currentPlayerName;
    }

    public Card getTopCardInPile() {
        return topCardInPile;
    }

    public void setTopCardInPile(Card topCardInPile) {
        this.topCardInPile = topCardInPile;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    public String getRoundState() {
        return roundState;
    }

    public void setRoundState(String roundState) {
        this.roundState = roundState;
    }

    public String getSetState() {
        return setState;
    }

    public void setSetState(String setState) {
        this.setState = setState;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public String getWinnerTeam() {
        return winnerTeam;
    }

    public void setWinnerTeam(String winnerTeam) {
        this.winnerTeam = winnerTeam;
    }

    public Map<String, List<String>> getTeamDisplayNames() {
        return teamDisplayNames;
    }

    public void setTeamDisplayNames(Map<String, List<String>> teamDisplayNames) {
        this.teamDisplayNames = teamDisplayNames;
    }

    public Map<String, Integer> getTeamScores() {
        return teamScores;
    }

    public void setTeamScores(Map<String, Integer> teamScores) {
        this.teamScores = teamScores;
    }
}