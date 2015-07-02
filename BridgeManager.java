package cardsuite;

public class BridgeManager extends Manager {
	
	public BridgeManager() {
		playerCount = 4;
		players = new HeartsPlayer[playerCount];
		
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		
		for (int i = 0; i < playerCount; i++) {
			players[i] = new HeartsPlayer();
			deck = players[i].fillHand(deck, random, roundCount);
		}
	}
	
	public static void main(String args[]) {
		
		
		
	}

}
