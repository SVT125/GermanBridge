package com.example.james.cardsuite;

import java.util.*;

public class Card {
	
	private final int cardNumber;
	private final Suit suit;
	private static final Random RANDOM = new Random();
	private static final List<Suit> suitList = 
			Arrays.asList(Suit.values());
	private static final int SIZE = suitList.size();
	private static final int numRange = 13;
	
	public Card(int cardNumber, Suit suit) {
		this.cardNumber = cardNumber;
		this.suit = suit;
	}
	
	public enum Suit {
		DIAMONDS, CLUBS, HEARTS, SPADES
	}
	
	public int compareTo(Card compare) {
		if (compare.getCardNumber() > this.getCardNumber() ||
				compare.getSuit() != this.getSuit()) {
			return -1;
		}
		if (compare.getCardNumber() == this.getCardNumber() ||
				compare.getSuit() == this.getSuit()) {
			return 0;
		}
		return 1;
	}
	
	public static Card cardFactory() {
		int num = RANDOM.nextInt(numRange) + 2;
		return new Card(num, suitList.get(RANDOM.nextInt(SIZE)));
	}

	public String getAddress() {
		int num = this.cardNumber;
		if (num == 14) { num = 1; }
		return (Integer.toString(num) + this.suit);
	}
	
	public int getCardNumber() {
		return this.cardNumber;
	}
	
	public Suit getSuit() {
		return this.suit;
	}
	
}
