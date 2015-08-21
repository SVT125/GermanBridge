package com.example.james.cardsuite;

import java.io.Serializable;

public class Card implements Serializable {
	public boolean isClicked = false;
	private final int cardNumber;
	private final Suit suit;
	
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
		if (compare.getCardNumber() == this.getCardNumber() &&
				compare.getSuit() == this.getSuit()) {
			return 0;
		}
		return 1;
	}

	public boolean equals(int cardNum, Suit cardSuit) {
		if (this.cardNumber == cardNum && suit.equals(cardSuit))
			return true;
		return false;
	}

	public String getAddress() {
		int num = this.cardNumber % 15;
		return (this.suit.toString().toLowerCase() + num);
	}
	
	public int getCardNumber() {
		return this.cardNumber;
	}
	
	public Suit getSuit() { return this.suit; }

	public String toString() {
		String string = Integer.toString(this.getCardNumber()) + this.getSuit();
		return string;
	}
	
}
