package com.example.james.cardsuite;

import java.util.Map.Entry;

public class BridgeManager extends Manager {

	private int addedGuesses = 0;
	private Card.Suit trumpSuit;
	private Card trumpCard; //Variable included in case want to display the trump card
	
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
			deck = players[i].fillHand(deck, random, potsFinished);
		}
		
		// find a trumpCard
		trumpCard = deck.remove(random.nextInt(deck.size()));
		trumpSuit = trumpCard.getSuit();
	}

	//Resets the state of the manager object for the next round.
	public void reset() {
		deck.clear();
		potsFinished++;
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		for (int i = 0; i < playerCount; i++) {
			players[i] = new BridgePlayer();
			deck = players[i].fillHand(deck, random, potsFinished);
		}
		trumpSuit = deck.remove(random.nextInt(deck.size())).getSuit();
		addedGuesses = 0;
	}

	//Ignoring out of bounds errors because we will take care of this in android studio - don't input out of bounds.
	public void potHandle() {
		
		// first player can choose any card he/she wants
		int chosen = scanner.nextInt();
		int currentPlayer = startPlayer;
		

		startSuit = players[startPlayer].hand.get(chosen).getSuit();
		pot.put(startPlayer, players[startPlayer].hand.remove(chosen));
		
		// other players choose cards
		for (int i = 0; i < playerCount - 1; i++) {
			currentPlayer = (currentPlayer+1) % playerCount;
			
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
			
			pot.put(currentPlayer, players[currentPlayer].hand.remove(chosen));
		}
		
		potAnalyze();
	}

	//Analyzes the pot and updates the pile count for the winning player of the pot.
	public void potAnalyze() {
		Card winCard = null;
		for (Entry<Integer, Card> entry : pot.entrySet()) {
			if (entry.getValue().getSuit() == startSuit) {
				if (winCard == null || entry.getValue().getCardNumber() > winCard.getCardNumber()) {
					winCard = entry.getValue();
					startPlayer = entry.getKey();
				}
			}
			// if a trump card exists it wins/gets compared to other trump cards
			else if (entry.getValue().getSuit() == trumpSuit) {
				if ((winCard == null || winCard.getSuit() != trumpSuit) || entry.getValue().getCardNumber() > winCard.getCardNumber()) {
					winCard = entry.getValue();
					startPlayer = entry.getKey();
				}
			}
		}
		// whoever wins pile gets one towards their obtained pile count
		((BridgePlayer)players[startPlayer]).obtained++;
	}
	
	public void guess() {
		int guess;
		int currentPlayer = startPlayer;
		
		for (int i = 0; i < playerCount; i++) {
			currentPlayer = (currentPlayer+1) % playerCount;
			
			// if last player, cannot select a number that is the addition of the other guesses
			if (i == playerCount - 1) {
				do {
					guess = scanner.nextInt();
				} while (guess >= potsFinished && (guess == potsFinished - addedGuesses) && guess < 0);
			}
			// other players can select any positive number lower than the max
			else {
				do {
					guess = scanner.nextInt();
				} while (guess >= potsFinished && guess < 0);
			}
			
			addedGuesses += guess;
			((BridgePlayer)players[currentPlayer]).guess = guess;
			currentPlayer++;
		}
	}

	public static void main(String args[]) {

		BridgeManager manager = new BridgeManager();

		// players guess how much they will win
		manager.guess();

		// players start putting cards into the pot and calculate score
		for (int i = 0; i < manager.potsFinished; i++) {
			manager.potHandle();
		}

		for (Player player : manager.players) {
			((BridgePlayer)player).scoreChange();
		}

		// resets deck, hands, etc. and increments round
		manager.reset();
	}
}
