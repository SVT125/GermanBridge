package com.example.james.cardsuite;

import android.app.Activity;
import android.widget.TextView;

import java.awt.font.TextAttribute;
import java.util.Map.Entry;

public class SpadesManager extends Manager {

	private boolean spadesBroken = false;
	public SpadesPlayer[] players;
	
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

	// not sure if this is correct
	public boolean isGameOver() {
		for(Player player : players) {
			if (player.score >= 500)
				return true;
			if (player.score <= -200)
				return false;
		}
		return false;
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

	public boolean potHandle(TextView output, int chosen, int currentPlayer, boolean initialOutputWritten, GameActivity activity) {
		if (!initialOutputWritten) {
			this.writeOutput(currentPlayer, output, activity);
			activity.initialOutputWritten = true;
			return false;
		}

		Card selectCard = players[currentPlayer].hand.remove(chosen);

		if(currentPlayer == startPlayer)
			startSuit = selectCard.getSuit();
		else if (!spadesBroken && (selectCard.getSuit().equals(Card.Suit.SPADES)))
			spadesBroken = true;

		pot.put(currentPlayer, selectCard);
		this.writeOutput((currentPlayer + 1) % 4, output, activity);
		return true;
	}

	public boolean cardSelectable(Card card, int currentPlayer) {
		if ((players[startPlayer].hand.size() == 13) && (card.compareTo(new Card(2, Card.Suit.CLUBS)) != 0)) {
			return false;
		}
		if (currentPlayer == startPlayer && (!spadesBroken && (card.getSuit().equals(Card.Suit.HEARTS)) || card.compareTo(new Card(12, Card.Suit.SPADES)) == 0)) {
			return false;
		}
		if (players[currentPlayer].hasSuit(startSuit) && pot.size() != 0) {
			if (!(card.getSuit().equals(startSuit))) {
				return false;
			}
		}
		return true;
	}

	public void writeOutput(int currentPlayer, TextView output, GameActivity activity) {
		players[currentPlayer].organize();
		activity.displayHands(currentPlayer);
		output.setText("Player " + Integer.toString(currentPlayer + 1) + " place a card");
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
