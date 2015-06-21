package cardsuite;
import java.util.*;

public class HeartsManager {
	
	private ArrayList<Card> deck = new ArrayList<Card>();
	private Random random = new Random();
	private int roundCount;
	private HeartsPlayer[] players;
	
	public HeartsManager() {
		this.roundCount = 1;
		
		for (int i = 1; i < 14; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		
		for (int i = 0; i < 4; i++) {
			players[i] = new HeartsPlayer();
			deck = players[i].fillHand(deck, random);
		}
		
	}
	
	public static void main (String args[]) {
		
	}
	
	public void swapCards(Player[] players, Card[] chosen) {
		
	}
	
}
