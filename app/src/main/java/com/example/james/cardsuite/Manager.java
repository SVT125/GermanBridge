package com.example.james.cardsuite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class Manager implements Serializable {
	
	protected ArrayList<Card> deck = new ArrayList<Card>();
	protected Map<Integer, Card> pot;
	protected Card.Suit startSuit;
	protected Random random = new Random();
	protected int startPlayer = 0, totalRoundCount, playerCount, potsFinished = 1, addedGuesses;
	protected Player[] players;
	protected List<Card> usedCards = new ArrayList<>();

	public abstract void potAnalyze();
	public abstract void potHandle(int chosen, int currentPlayer);
	public abstract void reset();
	public abstract boolean isGameOver();
	public abstract boolean cardSelectable(Card card, boolean finishedSwapping, int currentPlayer);
	public abstract List<Card> chooseCards(int playerNum, List<Integer> chosenIndices);
	public abstract void swapCards(Collection<?> chosen, int playerNum, int swapRound);
	public abstract int findStartPlayer();
	public abstract Player[] getPlayers();
	public abstract int findWinner();
	public int getPotsFinished() {
		return this.potsFinished;
	}
	public void newRound() { potsFinished++; }
	public boolean isInPlayersHands(int cardNumber, Card.Suit suit) {
		Card checkedCard = new Card(cardNumber,suit);
		for(Player player : players)
			for(Card card : player.hand)
				if(card.compareTo(checkedCard) == 0)
					return true;

		return false;
	}

	public boolean playerHasSuit(Card.Suit suit) {
		for(Player player : players)
			if(player.hasSuit(suit))
				return true;

		return false;
	}
}
