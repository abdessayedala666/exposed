public class SetState {
	private final Deck deck;
	private int activeRound;
	private String setStatus;
	private Team setWinner;

	public SetState() {
		this.deck = new Deck();
		this.deck.shuffle();
		this.activeRound = 1;
		this.setStatus = "ACTIVE";
	}

	public Deck getDeck() {
		return deck;
	}

	public int getActiveRound() {
		return activeRound;
	}

	public void setRoundsCompleted(int activeRound) {
		this.activeRound = activeRound;
	}

	public String getSetStatus() {
		return setStatus;
	}

	public void setSetStatus(String setStatus) {
		this.setStatus = setStatus;
	}

	public Team getSetWinner() {
		return setWinner;
	}

	public void setSetWinner(Team setWinner) {
		this.setWinner = setWinner;
	}
}
