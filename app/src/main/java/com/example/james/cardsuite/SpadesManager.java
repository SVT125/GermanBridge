package com.example.james.cardsuite;

import java.util.Map.Entry;

public class SpadesManager extends Manager {

	private boolean spadesBroken = false;
	private SpadesPlayer[] players;
	
	public SpadesManager() {
		
		playerCount = 4;
		players = new SpadesPlayer[playerCount];
		startPlayer = 0;
		
		// make deck
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		
		// give players cards via fillHand method
		for (int i = 0; i < playerCount; i++) {
			players[i] = new SpadesPlayer();
			deck = players[i].fillHand(deck, random, 13);
		}
		
	}
	
	public static void main(String args[]) {
		
		SpadesManager manager=  new SpadesManager();
		
		manager.bid();
		
		for (int i = 0; i < manager.potsFinished; i++) {
			manager.potHandle();
		}
		
		for (SpadesPlayer player : manager.players) {
			player.scoreChange();
		}
		
		manager.reset();
		
	}
	
	public void reset() {
		potsFinished++;
		spadesBroken = false;
		// make deck
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
				
		// give players cards via fillHand method
		for (int i = 0; i < playerCount; i++) {
			players[i] = new SpadesPlayer();
			deck = players[i].fillHand(deck, random, 13);
		}
	}
	
	public void potHandle() {
		
		Card startCard;
		int chosen;
		
		// first player move; cannot place spades
		do {
			chosen = scanner.nextInt();
		} while (players[startPlayer].hand.get(chosen).getSuit() == Card.Suit.SPADES);
		
		startCard = players[startPlayer].hand.remove(chosen);
		int currentPlayer = startPlayer;
		pot.put(startPlayer,startCard);
		startSuit = startCard.getSuit();
		
		for (int i = 0; i < 3; i++) {
			currentPlayer++;
			if (currentPlayer == playerCount) {
				currentPlayer = 0;
			}
			
			// must place down a card of the same suit
			if (players[currentPlayer].hasSuit(startSuit)) {
				do {
					chosen = scanner.nextInt();
				} while (players[startPlayer].hand.get(chosen).getSuit() != startSuit);
			}
			
			// if first turn and has none of same card, cannot place spades
			else if (players[currentPlayer].hand.size() == 13) {
				do {
					chosen = scanner.nextInt();
				} while (players[startPlayer].hand.get(chosen).getSuit() == Card.Suit.SPADES);
			}
			
			// otherwise can place anything
			else {
				chosen = scanner.nextInt();
				if (players[startPlayer].hand.get(chosen).getSuit() == Card.Suit.SPADES && !spadesBroken) {
					spadesBroken = true;
				}
			}
			
			pot.put(currentPlayer, players[currentPlayer].hand.remove(chosen));
		}
		
		potAnalyze();
		
	}
	
	public void potAnalyze() {
		Card winCard = null;
		for (Entry<Integer, Card> entry : pot.entrySet()) {
			if (entry.getValue().getSuit() == startSuit) {
				if (winCard == null) {
					winCard = entry.getValue();
					startPlayer = entry.getKey();
				}
				else if (entry.getValue().getCardNumber() > winCard.getCardNumber()){
					winCard = entry.getValue();
					startPlayer = entry.getKey();
				}
			}
			// if spades exists, it tops normal cards
			else if (entry.getValue().getSuit() == Card.Suit.SPADES) {
				if (winCard == null || winCard.getSuit() != Card.Suit.SPADES) {
					winCard = entry.getValue();
					startPlayer = entry.getKey();
				}
				else if (entry.getValue().getCardNumber() > winCard.getCardNumber()){
					winCard = entry.getValue();
					startPlayer = entry.getKey();
				}
			}
		}
		// CONDENSE SOMEHOW PLS
		players[startPlayer].obtained++;
		players[startPlayer].totalObtained++;
		if (startPlayer > 2) {
			players[startPlayer - 2].totalObtained++;
		}
		else {
			players[startPlayer + 2].totalObtained++;
		}
	}
	
	public void bid() {
		
		int bid;
		int currentPlayer = startPlayer;
		
		for (int i = 0; i < playerCount; i++) {
			
			if (currentPlayer == playerCount) {
				currentPlayer = 0;
			}
			
			do {
				bid = scanner.nextInt();
			} while (bid > -1 && bid < 14);
			
			players[currentPlayer].bid = bid;
			currentPlayer++;
		}
		
		// total bids added up for partners
		for(int i = 0; i < 4; i++)
			players[i].partnerBid = players[(i+2)%4].bid;
		
	}
	
}
