package com.example.james.cardsuite;
import android.util.Log;
import android.widget.TextView;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public class HeartsManager extends Manager implements Serializable {
	
	public boolean heartsBroken = false;
	
	public HeartsManager() {
		playerCount = 4;
		players = new HeartsPlayer[playerCount];
		pot = new HashMap<Integer, Card>();
		
		// an ace is 14 because it is higher than all of the other cards
		for (int i = 1; i < 14; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		
		for (int i = 0; i < 4; i++) {
			players[i] = new HeartsPlayer();
			deck = players[i].fillHand(deck, random, 13);
			players[i].organize();
		}
		
	}

	//Resets the state of the manager object for the next round.
	public void reset() {
		this.roundCount++;
		this.heartsBroken = false;
		for (int i = 1; i < 14; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		for (int i = 0; i < playerCount; i++) {
			deck = players[i].fillHand(deck, random, 13);
		}
	}

	//Returns true if the game is over, when someone has hit 100 points.
	public boolean isGameOver() {
		for(Player player : players)
			if(player.score >= 100)
				return true;

		return false;
	}

	//Returns the index of player with the lowest score.
	//Should not be called if the game is over, as there shouldn't be a winner yet.
	public int findWinner() {
		int lowestScore = Integer.MAX_VALUE, player = -1;
		for(int i = 0; i < 4; i++)
			if(players[i].score < lowestScore) {
				lowestScore = players[i].score;
				player = i;
			}

		return player;
	}

	//Called to handle every player's chosen card in the pot and analyzes the pot once all cards are put.
	//This method is only used for console testing, won't apply to the Android app - don't input out of bounds.
	public boolean potHandle(TextView output, int chosen, int currentPlayer, boolean initialOutputWritten, GameActivity activity) {
		Card selectCard = players[currentPlayer].hand.get(chosen);

		if (!initialOutputWritten) {
			this.writeOutput(currentPlayer, output, activity);
			activity.initialOutputWritten = true;
			return false;
		}

		if(currentPlayer == startPlayer) {
			// first move by first player - if it's the first turn with all hands of 13 cards, only 2 of clubs can be placed
			if (players[startPlayer].hand.size() == 13) {
				if (players[startPlayer].hand.get(chosen).compareTo(new Card(2, Card.Suit.CLUBS)) != 0) {
					output.setText("The first card placed can only be the 2 of clubs");
					return false;
				}

				selectCard = players[startPlayer].hand.remove(chosen);
				startSuit = selectCard.getSuit();
			} else {
				// if hearts is not broken, card cannot be hearts or queen of spades
				if (heartsBroken) {
					selectCard = players[startPlayer].hand.remove(chosen);
				} else {
					if (selectCard.getSuit().equals(Card.Suit.HEARTS) || selectCard.compareTo(new Card(12, Card.Suit.SPADES)) == 0) {
						output.setText("The first card placed cannot be hearts or the queen of spades");
						return false;
					}
					players[startPlayer].hand.remove(chosen);
				}
				startSuit = selectCard.getSuit();
			}
		} else {
		// after first move, other three players place their cards down
			// if player has a card of the same suit
			if (players[currentPlayer].hasSuit(startSuit)) {
				if(!selectCard.getSuit().equals(startSuit)) {
					output.setText("You need to place a card of the same suit as the pot");
					return false;
				}
				players[currentPlayer].hand.remove(chosen);
			}
			// otherwise if hand is 13, can play anything other than hearts
			else if (players[currentPlayer].hand.size() == 13) {
				if(selectCard.getSuit().equals(Card.Suit.HEARTS)) {
					output.setText("Hearts has not been broken yet");
					return false;
				}
				players[currentPlayer].hand.remove(chosen);
			}
			// if player does not have the same suit he can place anything
			else {
				if (selectCard.getSuit().equals(Card.Suit.HEARTS) ||
						selectCard.compareTo(new Card(12, Card.Suit.SPADES)) == 0 && heartsBroken == false) {
					heartsBroken = true;
				}
				selectCard = players[currentPlayer].hand.remove(chosen);
			}
		}
		pot.put(currentPlayer, selectCard);
		this.writeOutput((currentPlayer + 1) % 4, output, activity);
		return true;
	}

	public void writeOutput(int currentPlayer, TextView output, GameActivity activity) {
		players[currentPlayer].organize();
		activity.displayHands(currentPlayer);
		output.setText("Player " + Integer.toString(currentPlayer + 1) + " place a card");
	}

	//Analyzes the pot and determines the winning card and start player for the next round.
	public void potAnalyze() {
		Card winCard = null;
		for (Entry<Integer, Card> entry : pot.entrySet()) {
			if (entry.getValue().getSuit().equals(startSuit)) {
				if (winCard == null) {
					winCard = entry.getValue();
					startPlayer = entry.getKey();
				}
				else if (entry.getValue().getCardNumber() > winCard.getCardNumber()){
					winCard = entry.getValue();
					startPlayer = entry.getKey();
				}
			}
		}
	}

	//Determines the start player for the first round.
	public void findStartPlayer() {
		for (int j = 0; j < playerCount; j++) {
			for (int i = 0; i < players[j].hand.size(); i++) {
				if (players[j].hand.get(i).compareTo(new Card(2, Card.Suit.CLUBS)) == 0) {
					this.startPlayer = j;
				}
			}
		}
	}

	//Called for the swapping part of the round, alongside swapCards - returns the cards chosen of player index playerNum.
	//Assume chosen is an int array of 3 elements.
	public List<Card> chooseCards(int playerNum, List<Integer> chosenIndices) throws NullPointerException {
		
		List<Card> chosen = new ArrayList<Card>();
		for(int i = 0; i < 3; i++) {
			Card chosenCard = this.players[playerNum].hand.get(chosenIndices.get(i));
			try {
				if (chosen.contains(chosenCard))
					chosen.remove(chosenCard);
				else {
					chosen.add(chosenCard);
				}
			}
			catch (NullPointerException e) {
				throw e;
			}
		}
		return chosen;
	}

	//Called for the swapping part of the round, alongside swapCards - swaps the given cards for players.
	public void swapCards(Collection<?> chosen, int playerNum, int swapRound) {
		// The convention is that greater array indices means players to the left.
		players[playerNum].hand.removeAll(chosen);
		int otherPlayer = 0; //default initialized
		switch (swapRound) {
			case 0:
				otherPlayer = playerNum != 0 ? playerNum - 1 : 3;
				break;
			case 1:
				otherPlayer = playerNum != 3 ? playerNum + 1 : 0;
				break;
			case 2:
				otherPlayer = (playerNum == 0 || playerNum == 1) ? playerNum + 2 : playerNum - 2;
				break;
		}
		players[otherPlayer].hand.addAll((Collection<? extends Card>)chosen);
	}
}
