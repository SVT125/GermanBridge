package cardsuite;
import java.util.*;

public class HeartsManager {
	
	private ArrayList<Card> deck = new ArrayList<Card>();
	private Random random = new Random();
	private int roundCount;
	private final int playerCount = 4;
	private HeartsPlayer[] players = new HeartsPlayer[playerCount];
	private static Scanner scanner = new Scanner(System.in);
	private int startPlayer;
	private List<Card> pot;
	private Card.Suit startSuit;
	
	public HeartsManager() {
		this.roundCount = 1;
		
		for (int i = 1; i < 14; i++) {
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
		manager.potHandle(startPlayer);
		
	}
	
	public void potHandle(int startPlayer) {
		
		// first move by first player
		if (players[startPlayer].hand.size() == 13) {
		
			int select = scanner.nextInt();
			Card startCard = players[startPlayer].hand.get(select);
			while (startCard.compareTo(new Card(2, Card.Suit.CLUBS)) != 0) {
				select = scanner.nextInt();
				startCard = players[startPlayer].hand.get(select);
			}
			pot.add(startCard);
			startSuit = startCard.getSuit();
			
			// TODO
		
		}
		
		else {
			
			// TODO
			
		}
		
	}
	
	public int findStartPlayer() {
		outer: for (int j = 0; j < 4; j++) {
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
