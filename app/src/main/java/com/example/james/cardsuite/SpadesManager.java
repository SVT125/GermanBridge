package com.example.james.cardsuite;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class SpadesManager extends Manager implements Serializable {
	private boolean spadesBroken = false;
	
	public SpadesManager(int player) {
		
		playerCount = 4;
		players = new SpadesPlayer[playerCount];
		startPlayer = player;
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

		((SpadesPlayer)players[0]).partner = (SpadesPlayer)players[2];
		((SpadesPlayer)players[2]).partner = (SpadesPlayer)players[0];
		((SpadesPlayer)players[1]).partner = (SpadesPlayer)players[3];
		((SpadesPlayer)players[3]).partner = (SpadesPlayer)players[1];
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
		pot = new HashMap<Integer,Card>();
		spadesBroken = false;
		// make deck
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
				
		// give players cards via fillHand method
		for (int i = 0; i < playerCount; i++) {
			deck = players[i].fillHand(deck, random, 13);
			players[i].organize();
		}
		startPlayer = totalRoundCount % 4;
	}

	public int findStartPlayer() {
		this.startPlayer = (potsFinished - 1)% 4;
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

	public void potHandle(int chosen, int currentPlayer) {
		Card selectCard = players[currentPlayer].hand.remove(chosen);

		if(currentPlayer == startPlayer)
			startSuit = selectCard.getSuit();
		else if (!spadesBroken && (selectCard.getSuit().equals(Card.Suit.SPADES)))
			spadesBroken = true;

		pot.put(currentPlayer, selectCard);
	}

	public boolean cardSelectable(Card card, boolean finishedSwapping, int currentPlayer) {
		if (currentPlayer == startPlayer && !spadesBroken && players[currentPlayer].onlyHasSuit(Card.Suit.SPADES))
			return true;
		if (currentPlayer == startPlayer && !spadesBroken && card.getSuit().equals(Card.Suit.SPADES))
			return false;
		if (currentPlayer != startPlayer && players[currentPlayer].hasSuit(startSuit)) {
			if (!(card.getSuit().equals(startSuit))) {
				return false;
			}
		}
		return true;
	}

	public void addBids() {
		((SpadesPlayer)(getPlayers()[0])).totalBid = ((SpadesPlayer)(getPlayers()[0])).bid + ((SpadesPlayer)(getPlayers()[2])).bid;
		((SpadesPlayer)(getPlayers()[2])).totalBid = ((SpadesPlayer)(getPlayers()[0])).bid + ((SpadesPlayer)(getPlayers()[2])).bid;
		((SpadesPlayer)(getPlayers()[1])).totalBid = ((SpadesPlayer)(getPlayers()[1])).bid + ((SpadesPlayer)(getPlayers()[3])).bid;
		((SpadesPlayer)(getPlayers()[3])).totalBid = ((SpadesPlayer)(getPlayers()[1])).bid + ((SpadesPlayer)(getPlayers()[3])).bid;
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

		((SpadesPlayer)players[startPlayer]).obtained++;
		((SpadesPlayer)players[startPlayer]).totalObtained++;
		((SpadesPlayer)players[startPlayer]).partner.totalObtained++;

	}

	public Player[] getPlayers() { return this.players; }

	public List<Card> chooseCards(int playerNum, List<Integer> chosenIndices) {return null;};
	public void swapCards(Collection<?> chosen, int playerNum, int swapRound) {return;};
}
