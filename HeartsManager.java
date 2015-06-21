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
	
	public void swapCards(Player[] players, Collection<?> chosen, int playerNum) {
		
		// player 2 is to the left of player 1
		// might have a few errors
		switch (roundCount % 4) {
			case 0:
				if (playerNum != 3) {
					players[playerNum].hand.removeAll(chosen);
					players[playerNum + 1].hand.addAll((Collection<? extends Card>) chosen);
				}
				else {
					players[playerNum].hand.removeAll(chosen);
					players[0].hand.addAll((Collection<? extends Card>) chosen);
				}
				break;
			case 1:
				if (playerNum != 0) {
					players[playerNum].hand.removeAll(chosen);
					players[playerNum - 1].hand.addAll((Collection<? extends Card>) chosen);
				}
				else {
					players[playerNum].hand.removeAll(chosen);
					players[3].hand.addAll((Collection<? extends Card>) chosen);
				}
				break;
			case 2:
				if (playerNum == 0 || playerNum == 1) {
					players[playerNum].hand.removeAll(chosen);
					players[playerNum + 2].hand.addAll((Collection<? extends Card>) chosen);
				}
				else {
					players[playerNum].hand.removeAll(chosen);
					players[playerNum - 2].hand.addAll((Collection<? extends Card>) chosen);
				}
				break;
			case 3:
				break;
		}
	}
	
}
