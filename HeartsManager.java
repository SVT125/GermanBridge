package cardsuite;
import java.util.*;
import java.util.Map.Entry;

public class HeartsManager extends Manager {
	
	private boolean heartsBroken = false;
	private HeartsPlayer[] players;
	
	public HeartsManager() {
		playerCount = 4;
		players = new HeartsPlayer[playerCount];
		
		// an ace is 14 because it is higher than all of the other cards
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		
		for (int i = 0; i < 4; i++) {
			players[i] = new HeartsPlayer();
			deck = players[i].fillHand(deck, random, 13);
			players[i].organize();
		}
		pot = new HashMap<Card,Integer>();
	}
	
	public static void main (String args[]) {
		
		HeartsManager manager = new HeartsManager();
		System.out.println(manager);
		
		// choose and swap portion
		int swapRound = manager.roundCount % 4;
		if (swapRound != 3) {
			
			List<List<Card>> chosenLists = new ArrayList<List<Card>>();
			
			for (int i = 0; i < 4; i++) {
				System.out.println("Player " + Integer.toString(i + 1) + ": swappy swappy?");
				chosenLists.add(manager.chooseCards(i));
			}
			for (int i = 0; i < 4; i++) {
				manager.swapCards(chosenLists.get(i), i, swapRound);
				manager.players[i].organize();
			}
			System.out.println(manager);
		}
		
		// find player with 2 of clubs
		manager.findStartPlayer();
		
		// handles the pot stuff
		for (int i = 0; i < 13; i++) {
			manager.potHandle();
		}
		
		for (HeartsPlayer player : manager.players) {
			player.scoreChange();
		}
		manager.printScore();
		
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
		for (int i = 0; i < playerCount; i++) {
			deck = players[i].fillHand(deck, random, 13);
		}
	}
	
	public void potHandle() {
		
		int select;
		Card startCard;
		
		System.out.println("Player " + Integer.toString(startPlayer + 1) + ": place card pls");
		// first move by first player
		if (players[startPlayer].hand.size() == 13) {
			// not handling null pointer exception because this would not appear in the android app
			do {
				select = scanner.nextInt();
				
			} while (players[startPlayer].hand.get(select).compareTo(new Card(2, Card.Suit.CLUBS)) != 0);
			
			startCard = players[startPlayer].hand.remove(select);
			pot.put(startCard, startPlayer);
			startSuit = startCard.getSuit();
			
		}
		
		else {
			
			// if hearts is not broken, card cannot be hearts or queen of spades
			if (heartsBroken) {
				select = scanner.nextInt();
				startCard = players[startPlayer].hand.remove(select);
			}
			else {
				do {
					select = scanner.nextInt();
					startCard = players[startPlayer].hand.get(select);
				} while (startCard.getSuit().equals(Card.Suit.HEARTS) || startCard.compareTo(new Card(12,Card.Suit.SPADES)) == 0);
				players[startPlayer].hand.remove(select);
			}
			
			pot.put(startCard, startPlayer);
			startSuit = startCard.getSuit();
			
		}
		System.out.println(this);
		System.out.println(potString());
		
		// after first move, other three players place their cards down
		int currentPlayer = startPlayer;
		Card selectCard;
		for (int i = 0; i < playerCount - 1; i++) {
			currentPlayer++;
			// cleanup please
			if (currentPlayer == 4) {
				currentPlayer = 0;
			}
			
			System.out.println("Player " + Integer.toString(currentPlayer + 1) + ": place card pls");
			
			// if player has a card of the same suit
			if (players[currentPlayer].hasSuit(startSuit)) {
				do {
					select = scanner.nextInt();
					selectCard = players[currentPlayer].hand.get(select);
				} while (!(selectCard.getSuit().equals(startSuit)));
				players[currentPlayer].hand.remove(select);
			}
			
			// otherwise if hand is 13, can play anything other than hearts
			else if (players[currentPlayer].hand.size() == 13) {
				do {
					select = scanner.nextInt();
					selectCard = players[currentPlayer].hand.get(select);
				} while (selectCard.getSuit().equals(Card.Suit.HEARTS));
			}
			
			// if player does not have the same suit he can place anything
			else {
				select = scanner.nextInt();
				selectCard = players[currentPlayer].hand.remove(select);
				if (selectCard.getSuit().equals(Card.Suit.HEARTS) || 
					selectCard.compareTo(new Card(12, Card.Suit.SPADES)) == 0 && heartsBroken == false) {
					heartsBroken = true;
				}
			}
			
			pot.put(selectCard, currentPlayer);
			System.out.println(this);
			System.out.println(potString());
		}
		
		potAnalyze();
		for (Card c : pot.keySet()) {
			players[startPlayer].endPile.add(c);
		}
		this.pot.clear();
		System.out.println(this);
	}
	
	public void potAnalyze() {
		Card winCard = null;
		for (Entry<Card, Integer> entry : pot.entrySet()) {
			if (entry.getKey().getSuit().equals(startSuit)) {
				if (winCard == null) {
					winCard = entry.getKey();
					startPlayer = entry.getValue();
				}
				else if (entry.getKey().getCardNumber() > winCard.getCardNumber()){
					winCard = entry.getKey();
					startPlayer = entry.getValue();
				}
			}
		}
		System.out.println("Player " + Integer.toString(startPlayer + 1) + " wins!");
	}
	
	public void findStartPlayer() {
		for (int j = 0; j < playerCount; j++) {
			for (int i = 0; i < players[j].hand.size(); i++) {
				if (players[j].hand.get(i).compareTo(new Card(2, Card.Suit.CLUBS)) == 0) {
					this.startPlayer = j;
					return;
				}
			}
		}
	}
	
	public List<Card> chooseCards(int playerNum) {
		
		List<Card> chosen = new ArrayList<Card>();
		while (chosen.size() < 3) {
			int chose = scanner.nextInt();					
			Card chosenCard = this.players[playerNum].hand.get(chose);
			System.out.println("Chosen card: " + chose);
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
	
	public String printScore(){
		String s = "Scores:";
		s = s + "\n";
		for(Player p: players){
			int i = 1;
			s = s + "Player " + i + ": " + p.score + "\n";
			i++;
		}
		return s;
	}
	
	public String toString(){
		String hand = "";
		for (int i = 1; i <= playerCount; i++) {
			hand = hand + "Player " + Integer.toString(i) + ":";
			for (Card card : players[i - 1].hand) {
				hand = hand + " " + card.toString();
			}
			hand = hand + "\n";
		}
		return hand;
	}
	
	public String potString() {
		String potString = "Pot: ";
		for (Card c : pot.keySet()) {
			potString = potString + c.toString() + " ";
		}
		return potString;
	}
}
