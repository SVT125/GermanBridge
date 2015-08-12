package com.example.james.cardsuite;

import android.util.Log;
import android.widget.TextView;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class BridgeManager extends Manager implements Serializable {

	public int addedGuesses = 0;
	private Card.Suit trumpSuit;
	private Card trumpCard; //Variable included in case want to display the trump card

	public BridgeManager() {
		pot = new HashMap<Integer, Card>();
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
	public boolean potHandle(TextView output, int chosen, int currentPlayer, boolean initialOutputWritten, GameActivity activity) {
		// other players choose cards - must place similar suit to startSuit if they have it.
		pot.put(currentPlayer, players[currentPlayer].hand.get(chosen));

		if(currentPlayer == startPlayer) {
			// first player can choose any card he/she wants
			startSuit = players[startPlayer].hand.remove(chosen).getSuit();
			return true;
		}

		players[currentPlayer].hand.remove(chosen);
		return true;
	}

	//Returns true if the given card should be selectable to the player
	public boolean cardSelectable(Card card, boolean finishedSwapping, int currentPlayer) {
		return !(players[currentPlayer].hasSuit(startSuit) && card.getSuit() == startSuit) || (card.getSuit() == startSuit);
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

	public List<Card> chooseCards(int playerNum, List<Integer> chosenIndices) {return null;};
	public void swapCards(Collection<?> chosen, int playerNum, int swapRound) {return;};

	public Player[] getPlayers() { return this.players; }

	public int findStartPlayer() {
		this.startPlayer = potsFinished % 4 - 1;
		return this.startPlayer;
	}

	public int findWinner() {
		int max = 0, winner = -1;
		for (int i = 0; i < 4; i++) {
			if (players[i].score > max) {
				winner = i;
				max = players[i].score;
			}
		}
		return winner;
	}

	public boolean isGameOver() {
		if (totalRoundCount == (52/playerCount)) return true;
		return false;
	}
}
