package com.example.james.cardsuite;

import android.app.Activity;
import android.widget.TextView;

import java.awt.font.TextAttribute;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class SpadesManager extends Manager {

	private boolean spadesBroken = false;
	public SpadesPlayer[] players;
	
	public SpadesManager() {
		
		playerCount = 4;
		players = new SpadesPlayer[playerCount];
		startPlayer = 0;
		pot = new HashMap<Integer, Card>();
		
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
			players[i].organize();
		}

		players[0].partner = players[2];
		players[2].partner = players[0];
		players[1].partner = players[3];
		players[3].partner = players[1];
		
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
			players[i].organize();
		}
	}

	public int findStartPlayer() {
		this.startPlayer = potsFinished % 4 - 1;
		return this.startPlayer;
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

	public boolean cardSelectable(Card card, boolean finishedSwapping, int currentPlayer) {
		if (currentPlayer == startPlayer && (!spadesBroken && (card.getSuit().equals(Card.Suit.SPADES)))) {
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

		players[startPlayer].obtained++;
		players[startPlayer].totalObtained++;
		players[startPlayer].partner.totalObtained++;
	}

	public Player[] getPlayers() { return this.players; }

	public List<Card> chooseCards(int playerNum, List<Integer> chosenIndices) {return null;};
	public void swapCards(Collection<?> chosen, int playerNum, int swapRound) {return;};
}
