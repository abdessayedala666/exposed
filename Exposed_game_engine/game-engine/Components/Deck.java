import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
public class Deck {
    private final List<Card> cards = new ArrayList<>()  ;
    public Deck (){
        for (Suit suit : Suit.values()){
            for ( int  i = 1 ; i <= 10 ; i++ ){
                Card card = new Card(i, suit) ;
                cards.add(card) ; 
            }
        }
        Collections.shuffle(cards);

    }

    public List<Card> getCards(){
        return Collections.unmodifiableList(this.cards)  ;
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<Card> dealCards(int count) {
        if (cards.isEmpty() || count <= 0 ){
            throw new IllegalArgumentException("Invalid Arguments") ;
        }
        if(count > cards.size()) {
            throw new IllegalStateException("Not enough cards in deck");
        }
        List<Card> drawn = new ArrayList<>() ;

        for (int i = 0 ; i < count  ; i++ ){
            drawn.add(cards.remove(cards.size() -1 )) ;
        }
        return drawn ;
    }
    public boolean isEmpty(){
        return cards.isEmpty();
    }
    public int size(){
        return cards.size();
    }
    
}