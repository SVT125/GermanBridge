package com.example.james.cardsuite;

import java.io.Serializable;
import java.util.*;

public abstract class Player implements Serializable {
	
	protected List<Card> hand;
	protected int score, handsWon;
	protected boolean isBot = false;
	protected List<Integer> scoreHistory = new ArrayList<>();
	
	public abstract boolean scoreChange();
	
	public void organize() {
		Collections.sort(hand, new CardComparator());
	}
	
	public class CardComparator implements Comparator<Card> {
		
		@Override
		public int compare(Card a, Card b) {
			if (a.getSuit().equals(b.getSuit())) {
				return a.getCardNumber() - b.getCardNumber();
			}
			return a.getSuit().compareTo(b.getSuit());	
		}
		
	}

	public boolean onlyHasSuit(Card.Suit suit) {
		for (Card c : hand)
			if (!(c.getSuit().equals(suit)))
				return false;
		return true;
	}

	// Fills player hands with cards
	public ArrayList<Card> fillHand(ArrayList<Card> deck, Random random, int size) {
		for (int i = 0; i < size; i++) {
			hand.add(deck.remove(random.nextInt(deck.size())));
		}
		return deck;
	}

	public boolean hasSuit(Card.Suit suit) {
		for (Card card : hand)
			if (card.getSuit().equals(suit))
				return true;
		return false;
	}

	public boolean hasCard(int num, Card.Suit suit) {
		for (Card card1 : hand) {
			if (card1.equals(new Card(num, suit)))
				return true;
		}
		return false;
	}

	public int suitCount(Card.Suit suit) {
		int suitCount = 0;
		for (Card card : hand) {
			if (card.getSuit() == suit)
				suitCount++;
		}
		return suitCount;
	}
	
}
