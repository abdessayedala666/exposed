package com.game.exposed.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.game.exposed.dto.MoveResult;
import com.game.exposed.dto.Room;
import com.game.exposed.Exceptions.InvalidOperationException;
import com.game.exposed.game.Components.CaptureType;
import com.game.exposed.game.Components.Card;
import com.game.exposed.game.Components.GameEngineV2;
import com.game.exposed.game.Components.Player;
import com.game.exposed.game.Components.Team;

@Service
public class GameService {
    private final RoomService roomService;
    private final GameRegistry gameRegistry;
    
    public GameService(RoomService roomService, GameRegistry gameRegistry) {
        this.roomService = roomService;
        this.gameRegistry = gameRegistry;
    }

    private String toWsUser(Player player) {
        if (player == null || player.getSessionId() == null || player.getSessionId().isBlank()
                || player.getName() == null || player.getName().isBlank()) {
            return null;
        }
        return player.getSessionId() + ":" + player.getName();
    }

        public Map<String, MoveResult> launchGame(String roomId, String type) {

        System.out.println("===== launchGame() called =====");
        System.out.println("type = " + type);
        System.out.println("roomId = " + roomId);
        Room room = roomService.getRooms().get(roomId);
        Map<Integer, Player> playersBySeat = room.getSeats();
        Player player1 = playersBySeat.get(0);
        Player player2 = playersBySeat.get(1);
        Player player3 = playersBySeat.get(2);
        Player player4 = playersBySeat.get(3);
        
        Team teamOne = new Team(List.of(player1, player3));
        Team teamTwo = new Team(List.of(player2, player4));
        GameEngineV2 gameEngine = new GameEngineV2(teamOne, teamTwo);
        GameSession gameSession = new GameSession(roomId, gameEngine);
        gameRegistry.save(roomId, gameSession);
        System.out.println("game session saved for roomId = " + roomId);

        gameEngine.startGame();
        Map<String, MoveResult> launchSnapshotsByPlayerName = new HashMap<>();
        Player currentPlayer = gameEngine.getCurrentPlayer();
        if (currentPlayer == null) {
            throw new InvalidOperationException("Current player is not set in game engine");
        }
        Card topCardInpile = gameEngine.getGameState().getCurrentRound().getTopTableCard() ;
        String currentPlayerDisplayName = currentPlayer.getName();
        for (int seat = 0; seat < 4; seat++) {
            Player playerEntity = playersBySeat.get(seat);
            String playerName = playerEntity != null ? playerEntity.getName() : null;
            String wsUser = toWsUser(playerEntity);
            MoveResult snapshot = buildSnapshot(
                    roomId,
                    seat,
                    playerName,
                    playerEntity != null ? playerEntity.getHand() : null,
                    currentPlayerDisplayName,
                    topCardInpile,
                    gameEngine);
            if (wsUser != null && snapshot != null) {
                launchSnapshotsByPlayerName.put(wsUser, snapshot);
            }
        }
        
        System.out.println("Game launched for room: " + roomId);
        System.out.println("Current player = " + currentPlayerDisplayName);
        System.out.println("Player 1 hand: " + player1.getHand());
        System.out.println("Player 2 hand: " + player2.getHand());
        System.out.println("Player 3 hand: " + player3.getHand());
        System.out.println("Player 4 hand: " + player4.getHand());
        System.out.println("topCardInpile" + topCardInpile);
        return launchSnapshotsByPlayerName;
        }


    public MoveResult getCurrentStateForPlayer(String roomId, String playerName) {
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }
        if (playerName == null || playerName.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }

        GameSession session = gameRegistry.get(roomId);
        if (session == null) {
            return null;
        }

        GameEngineV2 gameEngine = session.getGameEngine();
        if (gameEngine == null) {
            return null;
        }

        Player currentPlayer;
        try {
            currentPlayer = gameEngine.getCurrentPlayer();
        } catch (IllegalStateException ex) {
            return null;
        }

        Player matchedPlayer = null;
        Integer matchedSeat = null;
        for (Player candidate : gameEngine.getGameState().getPlayers()) {
            if (candidate == null) {
                continue;
            }
            String displayName = candidate.getName();
            if (playerName.equals(displayName)) {
                matchedPlayer = candidate;
                matchedSeat = candidate.getIndex();
                break;
            }
        }

        if (matchedPlayer == null || matchedSeat == null) {
            return null;
        }
        Card topCardInpile = gameEngine.getGameState().getCurrentRound().getTopTableCard() ;

        return buildSnapshot(
            roomId,
            matchedSeat,
            matchedPlayer.getName(),
            matchedPlayer.getHand(),
                currentPlayer.getName(),
            topCardInpile,
            gameEngine);
    }

        private MoveResult buildSnapshot(
            String roomId,
            Integer seat,
            String playerName,
            List<Card> cards,
            String currentPlayerDisplayName,
            Card topCardInPile,
            GameEngineV2 gameEngine) {
        if (playerName == null || cards == null) {
            return null;
        }

        return new MoveResult(
            "INITIAL_HAND",
            roomId,
            seat,
            playerName,
            List.copyOf(cards),
            buildCapturedCardsByPlayerIndex(gameEngine),
            buildCardNumberByPayerIndex(gameEngine) ,
            currentPlayerDisplayName,
            topCardInPile,
            gameEngine.getGameState().getCurrentRound() != null ? gameEngine.getGameState().getCurrentRound().getRoundNumber() : null,
            getCurrentSetNumber(gameEngine),
            gameEngine.getGameState().getCurrentRound() != null ? gameEngine.getGameState().getCurrentRound().getRoundStatus() : null,
            gameEngine.getGameState().getCurrentSet() != null ? gameEngine.getGameState().getCurrentSet().getSetStatus() : null,
            gameEngine.getGameState().getGameStatus(),
            getWinnerTeamKey(gameEngine),
            buildTeamDisplayNames(gameEngine),
            buildTeamScores(gameEngine)
        );

        }
    private Map<String, List<String>> buildTeamDisplayNames(GameEngineV2 gameEngine) {
        return Map.of(
            "team1", List.of(
                gameEngine.getGameState().getTeams().get(0).getPlayer1().getName(),
                gameEngine.getGameState().getTeams().get(0).getPlayer2().getName()),
            "team2", List.of(
                gameEngine.getGameState().getTeams().get(1).getPlayer1().getName(),
                gameEngine.getGameState().getTeams().get(1).getPlayer2().getName())
        );
    }

    private Map<String, Integer> buildTeamScores(GameEngineV2 gameEngine) {
        return Map.of(
            "team1", gameEngine.getGameState().getTeams().get(0).getTotalScore(),
            "team2", gameEngine.getGameState().getTeams().get(1).getTotalScore()
        );
    }

    public Map<Integer,Integer> buildCardNumberByPayerIndex(GameEngineV2 gameEngine){
        Map<Integer ,Integer> cardNumberByPayerIndex  = new HashMap<>() ;
        gameEngine.getGameState().getPlayers().forEach(player ->{
            if(player != null){
                cardNumberByPayerIndex.put(player.getIndex() , player.getCardsNumber() ) ;}});
        return cardNumberByPayerIndex ;
            }
    private Map<Integer, List<Card>> buildCapturedCardsByPlayerIndex(GameEngineV2 gameEngine) {
        Map<Integer, List<Card>> capturedByPlayerIndex = new HashMap<>();
        gameEngine.getGameState().getPlayers().forEach(player -> {
            if (player != null) {
                capturedByPlayerIndex.put(player.getIndex(), List.copyOf(player.getCapturedCards()));
            }
        });
        return capturedByPlayerIndex;
    }

    private Integer getCurrentSetNumber(GameEngineV2 gameEngine) {
        if (gameEngine == null || gameEngine.getGameState() == null || gameEngine.getGameState().getCurrentSet() == null) {
            return null;
        }
        return gameEngine.getSetNumber();
    }

    private String getWinnerTeamKey(GameEngineV2 gameEngine) {
        if (gameEngine == null || gameEngine.getWinnerTeamOrNull() == null) {
            return null;
        }
        Team winner = gameEngine.getWinnerTeamOrNull();
        if (winner == gameEngine.getGameState().getTeams().get(0)) {
            return "team1";
        }
        if (winner == gameEngine.getGameState().getTeams().get(1)) {
            return "team2";
        }
        return null;
    }



    public Map<String, MoveResult> playTurn(String roomId,GameSession gameSession,Player playingPlayer,Player resolvedFacingPlayer,CaptureType captureType,Card card) {
        GameEngineV2 gameEngineV2 = gameSession.getGameEngine() ;
        List<Player> players = gameEngineV2.getGameState().getPlayers();
        System.out.println("the sessions are like this " + players);
        gameEngineV2.playTurn(playingPlayer, card, captureType, resolvedFacingPlayer);
        Player nextPlayer = gameEngineV2.getCurrentPlayer();
        String nextPlayerName = nextPlayer != null ? nextPlayer.getName() : null;
        Card topCardInPile = gameEngineV2.getGameState().getCurrentRound().getTopTableCard();
        Integer roundNumber = gameEngineV2.getGameState().getCurrentRound().getRoundNumber();
        Integer setNumber = getCurrentSetNumber(gameEngineV2);
        String roundState = gameEngineV2.getGameState().getCurrentRound().getRoundStatus();
        String setState = gameEngineV2.getGameState().getCurrentSet().getSetStatus();
        String gameState = gameEngineV2.getGameState().getGameStatus();
        String winnerTeam = getWinnerTeamKey(gameEngineV2);
        Map<String, List<String>> teamDisplayNames = buildTeamDisplayNames(gameEngineV2);
        Map<String, Integer> teamScores = buildTeamScores(gameEngineV2);
        Map<Integer, List<Card>> capturedCardByPlayer = buildCapturedCardsByPlayerIndex(gameEngineV2);
        Map<Integer , Integer> cardsNumberByPlayer = buildCardNumberByPayerIndex(gameEngineV2) ;
        Map<String, MoveResult> moveResultByPlayerName = new HashMap<>();
        players.forEach((playerEntity) -> {
            if (playerEntity == null || playerEntity.getName() == null) {
            return;
            }

            String wsUser = toWsUser(playerEntity);
            if (wsUser == null) {
                return;
            }

            List<Card> handForRecipient = List.copyOf(playerEntity.getHand());

            MoveResult recipientSnapshot = new MoveResult(
                "MOVE_UPDATE",
                roomId,
                playerEntity.getIndex(),
                playerEntity.getName(),
                handForRecipient,
                capturedCardByPlayer,
                cardsNumberByPlayer ,
                nextPlayerName,
                topCardInPile,
                roundNumber,
                setNumber,
                roundState,
                setState,
                gameState,
                winnerTeam,
                teamDisplayNames,
                teamScores);

            moveResultByPlayerName.put(wsUser, recipientSnapshot);
        });

        return moveResultByPlayerName;
        
    }



}



