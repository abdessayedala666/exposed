
public class Card   {
    private final int number ; 
    private final Suit suit ;
    public Card(int number , Suit suit ) throws IllegalArgumentException {
        if (number > 10 || number <= 0 ) {
            throw new IllegalArgumentException("number should be between 1 and 10!") ;
        }
        if(suit == null) {
            throw new IllegalArgumentException("the suit cannot be null !") ;

            
        }
        this.number = number ; 
        this.suit = suit ;
    }
    public int getNumber(){
        return this.number ;
    }

    public Suit getSuit(){
        return this.suit ;
    }
    @Override
    public String toString (){
        return number + " of " +  String.valueOf(suit).toLowerCase();
    }
    public int points(){
        if (number < 8) {
            return 5 ;
        }
        else {
            return 10 ;
        }
    }




}