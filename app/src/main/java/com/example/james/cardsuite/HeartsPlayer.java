package com.example.james.cardsuite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HeartsPlayer extends Player implements Serializable {
	
	// added endPile to count scores of each player at the end of every round
	protected List<Card> endPile;
	protected int roundScore = 0;
	boolean obtainedCards = false;
	
	public HeartsPlayer() {
		hand = new ArrayList<Card>();
		endPile = new ArrayList<Card>();
	}

	// boolean returns true or false value depending on whether player has shot the moon or not
	public boolean scoreChange() {
		if (roundScore == 26) {
			return true;
		}
		this.score += roundScore;
		scoreHistory.add(this.score);
		roundScore = 0;
		obtainedCards = false;
		return false;
	}

	public int tallyRoundScore() {
		for (Card card : endPile) {
			if (card.getSuit() == Card.Suit.HEARTS) {
				roundScore++;
			}
			else if (card.compareTo(new Card(12, Card.Suit.SPADES)) == 0) {
				roundScore += 13;
			}
		}
		endPile.clear();
		return roundScore;
	}
	
}
