package com.example.james.cardsuite;

import java.util.*;

public abstract class Player {
	
	protected List<Card> hand;
	protected int score, handsWon;
	protected boolean isBot = false;
	
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

	// Fills player hands with cards
	public ArrayList<Card> fillHand(ArrayList<Card> deck, Random random, int size) {
		for (int i = 0; i < size; i++) {
			hand.add(deck.remove(random.nextInt(deck.size())));
		}
		return deck;
	}

	// Returns whether a player has a certain suit or not
	public boolean hasSuit(Card.Suit suit) {
		if (suitCount(suit) == 0)
			return false;
		return true;
	}

	public boolean hasCard(int num, Card.Suit suit) {
		for (Card card1 : hand) {
			if (card1.equals(num, suit))
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
