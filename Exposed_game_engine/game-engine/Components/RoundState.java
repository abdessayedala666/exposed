import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoundState {
    private int roundNumber ;
    private int currentPlayerIndex ; 
    private List<Card> tablePile = new ArrayList<>() ;
    private String roundStatus = "ACTIVE"; // Can be "ACTIVE" or "FINISHED"

    public RoundState(int roundNumber){
        this.roundNumber = roundNumber ;
    }

    public List<Card>  getTablePile( ){
        return Collections.unmodifiableList(this.tablePile );
    }
    public void setTablePile(List<Card> pile){
        this.tablePile = pile ;
    }
    public int  getRoundNumber(){
        return roundNumber ;
    }
    public int getCurrentPlayerIndex(){
        return this.currentPlayerIndex ;
    }
    public void setRoundNumber(int roundNumber ){
        this.roundNumber = roundNumber ;
    }
    public void setCurrentPlayerIndex(int index) {
        this.currentPlayerIndex = index ;
    }
    public void addToTablePile(Card card){
        tablePile.add(card);
    }

    public Card getTopTableCard() {
        if (tablePile.isEmpty()) {
            throw new IllegalStateException("table pile is empty");
        }
        return tablePile.get(tablePile.size() - 1);
    }

    public Card removeTopTableCard() {
        if (tablePile.isEmpty()) {
            throw new IllegalStateException("table pile is empty");
        }
        return tablePile.remove(tablePile.size() - 1);
    }

    public void resetTablePile(){
        this.tablePile.clear(); 
    }
    public String getRoundStatus() {
        return roundStatus;
    }
    public void setRoundStatus(String status) {
        this.roundStatus = status;
    }
}
