package cardsuite;
import java.util.*;

public class HeartsPlayer extends Player {
	
	public HeartsPlayer() {
		hand = new ArrayList<Card>();
		this.score = 0;
	}
	
	public ArrayList<Card> fillHand(ArrayList<Card> deck, Random random) {
		for (int i = 0; i < 13; i++) {
			hand.add(deck.remove(random.nextInt(deck.size())));
		}
		return deck;
	}
	
}
