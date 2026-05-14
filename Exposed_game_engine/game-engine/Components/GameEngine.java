import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    private static final int WINNING_SCORE = 310;

    private List<Team> teams = new ArrayList<>() ;
    private GameState gameState;
    private SetState setState;
    private RoundState roundState ;

    public GameEngine(Team team1 , Team team2){
        teams.add(team1) ;
        teams.add(team2);
        // The engine needs a match container before set/round state can point to it.
        this.gameState = new GameState(teams, 0);
    } 
    public void startGame(){
        // startGame only kicks off the first set/round; the teams are already wired in the constructor.
        startSet();
        startRound(1);
    }
    public void startSet(){
        // A new set gets a fresh shuffled deck.
        setState = new SetState();
        this.gameState.setCurrentSet(this.setState);
    }
    public void startRound(int roundNumber){
        // Each round is a fresh state object so turn order and table pile reset cleanly.
        this.roundState = new RoundState(roundNumber);
        this.gameState.setCurrentRound(this.roundState);
        int playerCount = gameState.getPlayers().size();
        int dealerIndex = gameState.getCurrentDealerIndex();
        roundState.setCurrentPlayerIndex((dealerIndex + 1) % playerCount);
        distrbuteCards() ;
        if(roundState.getRoundNumber()==1){
        roundState.setTablePile(setState.getDeck().dealCards(4));
        }
    }
    public void distrbuteCards(){
        int dealerIndex = gameState.getCurrentDealerIndex();
        int playerCount = gameState.getPlayers().size();

        // Modulo rotation keeps the deal order moving clockwise from the dealer.
        for (int i = 0 ; i < playerCount ; i++){
            List<Card> drawnCards = setState.getDeck().dealCards(3) ;
            gameState.getPlayers().get((dealerIndex + i) % playerCount).setHand(drawnCards);
        }
    }
    public void playTurn(Player player , int chosenCardNumber , CaptureType captureType , Player facingPlayer){
        Card cardPlayed = player.playCard(chosenCardNumber) ;
        if (captureType == CaptureType.CAPTURE_FROM_PILE) {
            if(roundState.getTablePile().isEmpty()){
                roundState.addToTablePile(cardPlayed);
            }
            else{
                if(roundState.getTopTableCard().getNumber() == cardPlayed.getNumber()){
                    // Matching the table top captures both cards into the player's stack.
                    player.collectCards(List.of(cardPlayed ,roundState.removeTopTableCard() ));
                }
                else{
                    roundState.addToTablePile(cardPlayed);
                }
            }
        }
        else if (captureType == CaptureType.CAPTURE_FROM_OPPONENT){
            if(facingPlayer == null){
                throw new IllegalArgumentException("the opponent cannot be null") ;
            }

            if( facingPlayer.getTopcapturedCard().getNumber() != cardPlayed.getNumber() ){
                throw new IllegalArgumentException("cannot caputre the card , differnt numbe");
            }
            else{
                // Stealing the opponent means taking the whole top stack with that number.
                List<Card> capturedCards = facingPlayer.removeTopCapturedStack();
                capturedCards.add(cardPlayed);
                player.collectCards(capturedCards);
            }
    }
    }

    public void endRound(){
        //we end the round after all the players hands are empty(no other condition)
        //check if the hands are empty
        boolean empty = gameState.getPlayers().stream().allMatch(player -> player.getHand().isEmpty() == true) ;
        if (!empty){
            throw new IllegalStateException("a player still has card") ;
        }
        setState.setRoundsCompleted(roundState.getRoundNumber());

        if (roundState.getRoundNumber() == 1 || roundState.getRoundNumber() == 2) {
        //set didnt end yet
        //clear hands
        gameState.getPlayers().stream().forEach(player -> {
            player.clearHand();
        });
        //update th roundState round number
        startRound(roundState.getRoundNumber()+1);        
        }
        else{
        //the last round , the roundNumber == 3 
        //clear hands and clear the capturedCard List(will be in endSet , we still need it to calculate the scores)
        endSet();
        }
        //we update the round_count , we redistrubte the cards and thats it we pass to the next round

    }
    public void endSet(){
        if(roundState.getRoundNumber() != 3){
            throw new IllegalStateException("cannot end the set now") ; 
        }

        boolean allHandsEmpty = gameState.getPlayers().stream().allMatch(player -> player.getHand().isEmpty());
        if (!allHandsEmpty) {
            throw new IllegalStateException("cannot end set while a player still has cards in hand");
        }

        int teamOneSetDelta = teams.get(0).calculateSetScore();
        int teamTwoSetDelta = teams.get(1).calculateSetScore();

        // Persist set delta into team totals before we evaluate match winner.
        teams.get(0).CalculateTotalScore();
        teams.get(1).CalculateTotalScore();

        gameState.addSetResult(new GameState.SetResult(
                gameState.getSetResults().size() + 1,
                teamOneSetDelta,
                teamTwoSetDelta,
                teams.get(0).getTotalScore(),
                teams.get(1).getTotalScore()));

        // Captured stacks are round/set scoped and must be reset before next set.
        gameState.getPlayers().forEach(player -> {
            player.clearHand();
            player.clearCapturedCards();
        });

        setState.setRoundsCompleted(3);
        setState.setSetStatus("FINISHED");

        Team winner = null;
        int teamOneTotal = teams.get(0).getTotalScore();
        int teamTwoTotal = teams.get(1).getTotalScore();

        if (teams.get(0).hasWon(WINNING_SCORE) || teams.get(1).hasWon(WINNING_SCORE)) {
            // When both pass threshold, higher total wins; ties continue to next set.
            if (teamOneTotal > teamTwoTotal) {
                winner = teams.get(0);
            } else if (teamTwoTotal > teamOneTotal) {
                winner = teams.get(1);
            }
        }

        if (winner != null) {
            setState.setSetWinner(winner);
            gameState.setGameStatus("FINISHED");
            System.out.println("game finished: a team reached the winning score");
            return;
        }

        // No match winner yet: rotate dealer and start the next set from round 1.
        gameState.advanceDealerIndex();
        startSet();
        startRound(1);
    }
}
