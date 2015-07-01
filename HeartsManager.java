package cardsuite;
import java.util.*;
import java.util.Map.Entry;

public class HeartsManager {
	
	private ArrayList<Card> deck = new ArrayList<Card>();
	private Random random = new Random();
	private int roundCount;
	private final int playerCount = 4;
	private HeartsPlayer[] players = new HeartsPlayer[playerCount];
	private static Scanner scanner = new Scanner(System.in);
	private Map<Card, Integer> pot;
	private Card.Suit startSuit;
	private boolean heartsBroken;
	
	public HeartsManager() {
		this.roundCount = 1;
		
		// an ace is 14 because it is higher than all of the other cards
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		
		for (int i = 0; i < 4; i++) {
			players[i] = new HeartsPlayer();
			deck = players[i].fillHand(deck, random);
		}
		
	}
	
	public static void main (String args[]) {
		
		HeartsManager manager = new HeartsManager();
		
		// choose and swap portion
		int swapRound = manager.roundCount % 4;
		if (swapRound != 3) {
			
			List<List<Card>> chosenLists = new ArrayList<List<Card>>();
			
			for (int i = 0; i < 4; i++) {
				chosenLists.add(manager.chooseCards(i));
			}
			for (int i = 0; i < 4; i++) {
				manager.swapCards(chosenLists.get(i), i, swapRound);
			}
		}
		
		// find player with 2 of clubs
		int startPlayer = manager.findStartPlayer();
		
		// handles the pot stuff
		for (int i = 0; i < 13; i++) {
			startPlayer = manager.potHandle(startPlayer);
		}
		
		for (HeartsPlayer player : manager.players) {
			player.scoreChange();
		}
		
		// reshuffles deck and increments round count for next round
		manager.reset();
		
	}
	
	public void reset() {
		this.roundCount++;
		this.heartsBroken = false;
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		for (int i = 0; i < 4; i++) {
			deck = players[i].fillHand(deck, random);
		}
	}
	
	public int potHandle(int startPlayer) {
		
		int select;
		Card startCard;
		
		// first move by first player
		if (players[startPlayer].hand.size() == 13) {
			
			this.heartsBroken = false;
			
			// not handling null pointer exception because this would not appear in the android app
			do {
				select = scanner.nextInt();
				startCard = players[startPlayer].hand.get(select);
			} while (startCard.compareTo(new Card(2, Card.Suit.CLUBS)) != 0);
			
			pot.put(startCard, startPlayer);
			startSuit = startCard.getSuit();
			
		}
		
		else {
			
			// if hearts is not broken, card cannot be hearts or queen of spades
			if (heartsBroken) {
				select = scanner.nextInt();
				startCard = players[startPlayer].hand.get(select);
			}
			else {
				do {
					select = scanner.nextInt();
					startCard = players[startPlayer].hand.get(select);
				} while (startCard.getSuit() != Card.Suit.HEARTS || startCard.compareTo(new Card(12,Card.Suit.SPADES)) != 0);
			}
			
			pot.put(startCard, startPlayer);
			startSuit = startCard.getSuit();
			
		}
		
		// after first move, other three players place their cards down
		int currentPlayer;
		Card selectCard;
		for (int i = 0; i < 3; i++) {
			currentPlayer = startPlayer++;
			// cleanup please
			if (currentPlayer == 4) {
				currentPlayer = 0;
			}
			
			// if player has a card of the same suit
			if (players[currentPlayer].hasSuit(startSuit)) {
				do {
					select = scanner.nextInt();
					selectCard = players[startPlayer].hand.get(select);
				} while (startCard.getSuit() != startSuit);
			}
			// if player does not have the same suit he can place anything
			else {
				select = scanner.nextInt();
				selectCard = players[startPlayer].hand.get(select);
			}
			
			pot.put(selectCard, currentPlayer);
			
		}
		
		startPlayer = potAnalyze(startSuit);
		players[startPlayer].endPile.addAll((Collection<? extends Card>) pot);
		this.pot.clear();
		
		return startPlayer;
	}
	
	public int potAnalyze(Card.Suit startSuit) {
		int winner = 0;
		Card winCard = null;
		for (Entry<Card, Integer> entry : pot.entrySet()) {
			if (entry.getKey().getSuit() == startSuit) {
				if (winCard == null) {
					winCard = entry.getKey();
					winner = entry.getValue();
				}
				else if (entry.getKey().getCardNumber() > winCard.getCardNumber()){
					winCard = entry.getKey();
					winner = entry.getValue();
				}
			}
		}
		return winner;
	}
	
	public int findStartPlayer() {
		for (int j = 0; j < 4; j++) {
			for (int i = 0; i < players[j].hand.size(); i++) {
				if (players[j].hand.get(i).compareTo(new Card(2, Card.Suit.CLUBS)) == 0) {
					return j;
				}
			}
		}
		return 0;
	}
	
	public List<Card> chooseCards(int playerNum) {
		
		List<Card> chosen = new ArrayList<Card>();
		while (chosen.size() < 4) {
			int chose = scanner.nextInt();					
			Card chosenCard = this.players[playerNum].hand.get(chose);
			System.out.println("Chose card: " + chose);
		try {
			if (chosen.contains(chosenCard))
				chosen.remove(chosenCard);
			else {
				chosen.add(chosenCard);
			}
		}
		catch (NullPointerException e) {
			System.out.println("Card out of bounds!");
			}
		}
		return chosen;
	}
	
	public void swapCards(Collection<?> chosen, int playerNum, int swapRound) {
		// The convention is that greater array indices means players to the left.
		players[playerNum].hand.removeAll(chosen);
		int otherPlayer = 0; //default initialized
		switch (swapRound) {
			case 0:
				otherPlayer = playerNum != 3 ? playerNum + 1 : 0;
				break;
			case 1:
				otherPlayer = playerNum != 0 ? playerNum - 1 : 3;
				break;
			case 2:
				otherPlayer = (playerNum == 0 || playerNum == 1) ? playerNum + 2 : playerNum - 2;
				break;
		}
		players[otherPlayer].hand.addAll((Collection<? extends Card>)chosen);
	}
	
}
