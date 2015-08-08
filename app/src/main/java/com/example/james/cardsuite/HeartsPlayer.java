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

	// boolean returns true or false value depending on whether player has shot the moon or not
	public boolean scoreChange() {
		int roundScore = 0;
		for (Card card : endPile) {
			if (card.getSuit() == Card.Suit.HEARTS) {
				this.score++;
				roundScore++;
			}
			else if (card.compareTo(new Card(12, Card.Suit.SPADES)) == 0) {
				this.score += 13;
				roundScore += 13;
			}
		}
		endPile.clear();
		if (roundScore == 26) {
			this.score -= 26;
			return true;
		}
		return false;
	}
	
	
}
