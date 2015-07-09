package cardsuite;

import java.util.*;

public abstract class Player {
	
	protected List<Card> hand;
	protected int score;
	
	public abstract void scoreChange();
	
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
	
	public ArrayList<Card> fillHand(ArrayList<Card> deck, Random random, int size) {
		for (int i = 0; i < size; i++) {
			hand.add(deck.remove(random.nextInt(deck.size())));
		}
		return deck;
	}
	
	public boolean hasSuit(Card.Suit suit) {
		for (Card card : hand) {
			if (card.getSuit().equals(suit)) {
				return true;
			}
		}
		return false;
	}
	
}
