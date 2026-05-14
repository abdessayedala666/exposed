import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Player {
    /** The player's display name. */
    private String name;
    public int index ;
    /** The player's current hand of cards. */
    private final List<Card> hand = new ArrayList<>();
    /** The cards this player has captured during the round. */
    private final List<Card> capturedCards = new ArrayList<>();
    /**
    * Constructs a new player with the given name.
    * @param name the player's name
    */
    public Player(String name , int index ) {
        this.name = name;
        this.index = index ;
        }

    /**
    * Gets the player's name.
    * @return the player's name
    */
    public String getName() {
        return this.name;
        }
    public int getIndex(){
        return this.index ;
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
    * Sets the player's hand for a new round. Hand must be empty before setting.
    * @param newHand the new hand of cards
    * @throws IllegalStateException if the hand is not empty
    */
    public void setHand(List<Card> newHand) {
        if (!this.hand.isEmpty()) {
            throw new IllegalStateException("cannot draw cards, hand is not empty");
            }
            hand.clear();
            hand.addAll(newHand);
            }

    /**
    * Plays a card from the hand by 1-based index (for user-facing input).
    * @param cardNumber the 1-based index of the card to play
    * @return the card played
    * @throws IllegalStateException if hand is empty
    * @throws IllegalArgumentException if cardNumber is out of bounds
    */
    public Card playCard(int cardNumber) {
        if (hand.isEmpty()) {
            throw new IllegalStateException("hand is empty, can't play cards!");
                }
            if (cardNumber < 1 || cardNumber > hand.size()) {
                throw new IllegalArgumentException("Invalid card number");
                }
                return hand.remove(cardNumber - 1);
            }

    /**
    * Checks if the player is "exposed" (has captured no cards this round).
    * @return true if no cards have been captured, false otherwise
    */
    public boolean isExposed() {
        return capturedCards.isEmpty();
        }
    /**
    * Adds a set of captured cards to the player's collection.
    * All cards must have the same number.
    * @param cards the cards to add
    * @throws IllegalStateException if not all cards have the same number
    */
    public void collectCards(List<Card> cards) {
        boolean allSame = cards.stream()
            .allMatch(card -> card.getNumber() == cards.get(0).getNumber());
            if (!allSame) {
                 throw new IllegalStateException("all cards must have same number");
                }
            this.capturedCards.addAll(cards);
            }

    /**
    * Calculates the player's score for the round.
    * @return the score (with -50 penalty if exposed)
    */
    public int calculateScore() {
        if (isExposed()) {
            return -50;
            }
        int total = 0;
        for (Card card : capturedCards) {
            total += card.points();
            }
            return total;
            }

    /**
    * Clears the player's hand for a new round.
    */
    public void clearHand() {
        this.hand.clear();
        }

    /**
    * Clears the player's captured cards for a new round.
    */
    public void clearCapturedCards() {
        this.capturedCards.clear();
        }

    /**
    * Gets a read-only view of the player's hand.
    * @return an unmodifiable list of cards in hand
    */
    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
        }
    /**
    * Gets a read-only view of the player's captured cards.
    * @return an unmodifiable list of captured cards
    */
    public List<Card> getCapturedCards() {
        return Collections.unmodifiableList(capturedCards);
        }

    public Card getTopcapturedCard() {
        if (capturedCards.isEmpty()) {
            throw new IllegalStateException("capturedCards is empty");
        }
        return capturedCards.get(capturedCards.size() - 1);
    }

    public List<Card> removeTopCapturedStack() {
        if (capturedCards.isEmpty()) {
            throw new IllegalStateException("capturedCards is empty");
        }
        int topNumber = getTopcapturedCard().getNumber();
        List<Card> removedStack = new ArrayList<>();

        while (!capturedCards.isEmpty() && getTopcapturedCard().getNumber() == topNumber) {
            removedStack.add(0, capturedCards.remove(capturedCards.size() - 1));
        }

        return removedStack;
    }
        }
