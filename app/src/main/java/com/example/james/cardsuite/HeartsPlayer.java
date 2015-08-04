package com.example.james.cardsuite;
import java.util.*;

public class HeartsPlayer extends Player {
	
	// added endPile to count scores of each player at the end of every round
	protected List<Card> endPile;
	
	public HeartsPlayer() {
		hand = new ArrayList<Card>();
		endPile = new ArrayList<Card>();
		this.score = 0;
	}
	
	public void scoreChange() {
		for (Card card : endPile) {
			if (card.getSuit() == Card.Suit.HEARTS) {
				this.score++;
			}
			else if (card.compareTo(new Card(12, Card.Suit.SPADES)) == 0) {
				this.score = score + 13;
			}
		}
		endPile.clear();
	}
	
	
}
