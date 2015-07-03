package cardsuite;

import java.util.Map.Entry;

public class BridgeManager extends Manager {
	
	private BridgePlayer players[];
	private int addedGuesses = 0;
	private Card.Suit trumpSuit;
	
	public BridgeManager() {
		playerCount = 4;
		players = new BridgePlayer[playerCount];
		startPlayer = 0;
		
		// make deck
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		
		// give players cards via fillHand method
		for (int i = 0; i < playerCount; i++) {
			players[i] = new BridgePlayer();
			deck = players[i].fillHand(deck, random, roundCount);
		}
		
		// find a trumpCard
		trumpSuit = deck.remove(random.nextInt(deck.size())).getSuit();
	}
	
	public static void main(String args[]) {
		
		BridgeManager manager = new BridgeManager();
		
		// players guess how much they will win
		manager.guess();
		
		// players start putting cards into the pot and calculate score
		for (int i = 0; i < manager.roundCount; i++) {
			manager.potHandle();
		}
		
		for (BridgePlayer player : manager.players) {
			player.scoreChange();
		}
		
		// resets deck, hands, etc. and increments round
		manager.reset();
		
	}
	
	public void reset() {
		deck.clear();
		roundCount++;
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		for (int i = 0; i < playerCount; i++) {
			players[i] = new BridgePlayer();
			deck = players[i].fillHand(deck, random, roundCount);
		}
		trumpSuit = deck.remove(random.nextInt(deck.size())).getSuit();
		addedGuesses = 0;
	}
	
	public void potHandle() {
		
		// first player can choose any card he/she wants
		int chosen = scanner.nextInt();
		
		// ignoring out of bounds errors because we will take care of this in android studio
		// just don't guess out of bounds when we are testing
		pot.put(players[startPlayer].hand.get(chosen), startPlayer);
		startSuit = players[startPlayer].hand.get(chosen).getSuit();
		
		int currentPlayer = startPlayer;
		
		// other players choose cards
		for (int i = 0; i < playerCount - 1; i++) {
			currentPlayer++;
			if (currentPlayer == playerCount) {
				currentPlayer = 0;
			}
			
			// player must place similar suit to startSuit if he has it
			if (players[currentPlayer].hasSuit(startSuit)) {
				do {
					chosen = scanner.nextInt();
				} while (players[startPlayer].hand.get(chosen).getSuit() != startSuit);
			}
			
			// otherwise he places anything
			else {
				chosen = scanner.nextInt();
			}
			
			pot.put(players[currentPlayer].hand.get(chosen), currentPlayer);
		}
		
		potAnalyze();
		
	}
	
	public void potAnalyze() {
		Card winCard = null;
		for (Entry<Card, Integer> entry : pot.entrySet()) {
			if (entry.getKey().getSuit() == startSuit) {
				if (winCard == null) {
					winCard = entry.getKey();
					startPlayer = entry.getValue();
				}
				else if (entry.getKey().getCardNumber() > winCard.getCardNumber()){
					winCard = entry.getKey();
					startPlayer = entry.getValue();
				}
			}
			
			// if a trump card exists it wins/gets compared to other trump cards
			else if (entry.getKey().getSuit() == trumpSuit) {
				if (winCard == null) {
					winCard = entry.getKey();
					startPlayer = entry.getValue();
				}
				else if (entry.getKey().getCardNumber() > winCard.getCardNumber()){
					winCard = entry.getKey();
					startPlayer = entry.getValue();
				}
			}
		}
		// whoever wins pile gets one towards their obtained pile count
		players[startPlayer].obtained++;
	}
	
	public void guess() {
		int guess;
		int currentPlayer = startPlayer;
		
		for (int i = 0; i < playerCount; i++) {
			
			if (currentPlayer == playerCount) {
				currentPlayer = 0;
			}
			
			// if last player, cannot select a number that is the addition of the other guesses
			if (i == playerCount - 1) {
				do {
					guess = scanner.nextInt();
				} while (guess >= roundCount && (guess == roundCount - addedGuesses) && guess < 0);
			}
			
			// other players can select any positive number lower than the max
			else {
				do {
					guess = scanner.nextInt();
				} while (guess >= roundCount && guess < 0);
			}
			
			addedGuesses = addedGuesses + guess;
			players[currentPlayer].guess = guess;
			currentPlayer++;
			
		}
	}

}
