package blackjack;

public class Card {
	public Suit suit;
	public Value value;
	
	public Card(Suit suit, Value value) {
		this.value = value;
		this.suit = suit;
	}

	public String toString() {
		return this.suit.toString() + "-" + this.value.toString();
	}
	public Value getValue() {
		return this.value;
	}
	
	public Suit getSuit() {
		return this.suit;
	}
 }

