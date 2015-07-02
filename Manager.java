package cardsuite;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public abstract class Manager {
	
	protected ArrayList<Card> deck = new ArrayList<Card>();
	protected Random random = new Random();
	protected int playerCount;
	protected int roundCount = 1;
	protected HeartsPlayer[] players;
	protected static Scanner scanner = new Scanner(System.in);
	protected Map<Card, Integer> pot;
	protected Card.Suit startSuit;
	
	public abstract int potHandle(int startPlayer);
	public abstract int potAnalyze(Card.Suit startSuit);
	public abstract void reset();
	
}
