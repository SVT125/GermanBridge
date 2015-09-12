package com.example.james.cardsuite;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HeartsManager extends Manager implements Serializable {
	public boolean heartsBroken = false;

	public HeartsManager(boolean[] isBot) {
		playerCount = 4;
		players = new HeartsPlayer[playerCount];
		pot = new HashMap<Integer, Card>();

		// an ace is 14 because it is higher than all of the other cards
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}

		for (int i = 0; i < 4; i++) {
			if (isBot[i]) {
				players[i] = new HeartsAI(0);
			}
			else {
				players[i] = new HeartsPlayer();
			}

			deck = players[i].fillHand(deck, random, 13);
			players[i].organize();
		}

	}

	//Resets the state of the manager object for the next round.
	public void reset() {
		this.heartsBroken = false;
		this.potsFinished++;
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		for (int i = 0; i < playerCount; i++) {
			deck = players[i].fillHand(deck, random, 13);
			players[i].organize();
		}
		usedCards.clear();
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

	// Called to check whether a card should be selectable by a player or not
	public boolean cardSelectable(Card card, boolean finishedSwapping, int currentPlayer) {
		// all swappable cards are selectable during the swapping process
		if (finishedSwapping) {
			// if first turn, only 2 of clubs can be selected
			if ((players[startPlayer].hand.size() == 13) && (card.compareTo(new Card(2, Card.Suit.CLUBS)) != 0)) {
				return false;
			}
			if (currentPlayer == startPlayer && !heartsBroken && players[currentPlayer].onlyHasSuit(Card.Suit.HEARTS))
				return true;
			if (currentPlayer == startPlayer && (!heartsBroken && ((card.getSuit().equals(Card.Suit.HEARTS)) || card.compareTo(new Card(12, Card.Suit.SPADES)) == 0))) {
				return false;
			}
			if (players[currentPlayer].hasSuit(startSuit) && pot.size() != 0) {
				if (!(card.getSuit().equals(startSuit))) {
					return false;
				}
			}
		}
		return true;
	}

	//Called to handle every player's chosen card in the pot and analyzes the pot once all cards are put.
	//This method is only used for console testing, won't apply to the Android app - don't input out of bounds.
	public void potHandle(int chosen, int currentPlayer) {
		Card selectCard = players[currentPlayer].hand.remove(chosen);

		if(currentPlayer == startPlayer)
			startSuit = selectCard.getSuit();
		else if (!heartsBroken && (selectCard.getSuit().equals(Card.Suit.HEARTS) || selectCard.compareTo(new Card(12, Card.Suit.SPADES)) == 0))
			heartsBroken = true;

		pot.put(currentPlayer, selectCard);
	}

	public void potHandle(Card chosen, int currentPlayer) {
		((HeartsAI)players[currentPlayer]).removeCard(chosen);

		if(currentPlayer == startPlayer)
			startSuit = chosen.getSuit();
		else if (!heartsBroken && (chosen.getSuit().equals(Card.Suit.HEARTS)))
			heartsBroken = true;

		pot.put(currentPlayer, chosen);
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
	public int findStartPlayer() {
		for (int j = 0; j < playerCount; j++) {
			for (int i = 0; i < players[j].hand.size(); i++) {
				if (players[j].hand.get(i).compareTo(new Card(2, Card.Suit.CLUBS)) == 0) {
					this.startPlayer = j;
				}
			}
		}
		return startPlayer;
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
		players[otherPlayer].organize();
	}

	public boolean potHasSuit(Card.Suit suit) {
		for (Card card : pot.values())
			if (card.getSuit().equals(suit))
				return true;
		return false;
	}

	public int potHighestValue() {
		int max = 0;
		for (Card card : pot.values())
			if (card.getCardNumber() > max && card.getSuit().equals(startSuit))
				max = card.getCardNumber();
		return max;
	}

	public Card.Suit leastPlayedSuit(boolean heartsBroken) {
		Map<Integer, Card.Suit> suitCounts = new HashMap<>();
		int clubs = 0, diamonds = 0, hearts = 0, spades = 0;
		for (Card card : usedCards) {
			if (card.getSuit().equals(Card.Suit.SPADES))
				spades++;
			if (card.getSuit().equals(Card.Suit.HEARTS))
				hearts++;
			if (card.getSuit().equals(Card.Suit.CLUBS))
				clubs++;
			if (card.getSuit().equals(Card.Suit.DIAMONDS))
				diamonds++;
		}
		if (heartsBroken)
			suitCounts.put(hearts, Card.Suit.HEARTS);
		suitCounts.put(diamonds, Card.Suit.DIAMONDS);
		suitCounts.put(clubs, Card.Suit.CLUBS);

		int min = spades;
		Card.Suit minSuit = Card.Suit.SPADES;
		for (Entry<Integer, Card.Suit> entry : suitCounts.entrySet()) {
			if (entry.getKey() < min ) {
				min = entry.getKey();
				minSuit = entry.getValue();
			}
		}
		return minSuit;
	}
	
	public Player[] getPlayers() { return this.players; }
}
