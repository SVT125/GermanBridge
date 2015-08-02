package com.example.james.cardsuite;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public abstract class Manager {
	
	protected ArrayList<Card> deck = new ArrayList<Card>();
	protected Random random = new Random();
	protected int playerCount;
	protected int roundCount = 1;
	protected static Scanner scanner = new Scanner(System.in);
	protected Map<Card, Integer> pot;
	protected Card.Suit startSuit;
	protected int startPlayer;
	protected Player[] players;

	public abstract void potAnalyze();
	public abstract void reset();
	public int getRoundCount() {
		return this.roundCount;
	}
}
