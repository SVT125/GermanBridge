package cardsuite;
import java.util.*;

public class HeartsPlayer extends Player {
	
	// added endPile to count scores of each player at the end of every round
	protected List<Card> endPile;
	
	public HeartsPlayer() {
		hand = new ArrayList<Card>();
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
	
	public ArrayList<Card> fillHand(ArrayList<Card> deck, Random random) {
		for (int i = 0; i < 13; i++) {
			hand.add(deck.remove(random.nextInt(deck.size())));
		}
		return deck;
	}
	
	public void organize() {
		Collections.sort(hand, new CardComparator());
	}
	
	public boolean hasSuit(Card.Suit suit) {
		for (Card card : hand) {
			if (card.getSuit() == suit) {
				return true;
			}
		}
		return false;
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
}
