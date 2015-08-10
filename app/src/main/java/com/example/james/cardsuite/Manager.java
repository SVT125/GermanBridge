package com.example.james.cardsuite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.widget.TextView;

public abstract class Manager {
	
	protected ArrayList<Card> deck = new ArrayList<Card>();
	protected Map<Integer, Card> pot;
	protected Card.Suit startSuit;
	protected Random random = new Random();
	protected int startPlayer = 0, totalRoundCount, playerCount, potsFinished = 1, addedGuesses;
	protected Player[] players;

	public abstract void potAnalyze();
	public abstract boolean potHandle(TextView output, int chosen, int currentPlayer, boolean initialOutputWritten, GameActivity activity);
	public abstract void reset();
	public abstract boolean isGameOver();
	public abstract boolean cardSelectable(Card card, boolean finishedSwapping, int currentPlayer);
	public abstract List<Card> chooseCards(int playerNum, List<Integer> chosenIndices);
	public abstract void swapCards(Collection<?> chosen, int playerNum, int swapRound);
	public abstract int findStartPlayer();
	public int getPotsFinished() {
		return this.potsFinished;
	}
	public void newRound() { potsFinished++; }
}
