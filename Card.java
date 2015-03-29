package germanbridge;

import java.util.*;

public class Card {
	
	private final int cardNumber;
	private final Suit suit;
	private static final Random RANDOM = new Random();
	private static final List<Suit> suitList = 
			Arrays.asList(Suit.values());
	private static final int SIZE = suitList.size();
	private static final int numRange = 12;
	private static final int numGap = 2;
	
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
		return 1;
	}
	
	public Card cardFactory() {
		int num = RANDOM.nextInt(numRange) + numGap;
		return new Card(num, suitList.get(RANDOM.nextInt(SIZE)));
	}
	
	public int getCardNumber() {
		return this.cardNumber;
	}
	
	public Suit getSuit() {
		return this.suit;
	}
}
