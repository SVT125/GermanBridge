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
		manager.findStartPlayer();
		
		// handles the pot stuff
		for (int i = 0; i < 13; i++) {
			manager.potHandle();
			toString();
		}
		
		for (HeartsPlayer player : manager.players) {
			player.scoreChange();
			printScore();
			
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
		for (int i = 0; i < playerCount; i++) {
			deck = players[i].fillHand(deck, random, 13);
		}
	}
	
	public void potHandle() {
		
		int select;
		Card startCard;
		
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
				} while (startCard.getSuit() == Card.Suit.HEARTS || startCard.compareTo(new Card(12,Card.Suit.SPADES)) == 0);
				players[startPlayer].hand.remove(select);
			}
			
			pot.put(startCard, startPlayer);
			startSuit = startCard.getSuit();
			
		}
		
		// after first move, other three players place their cards down
		int currentPlayer = startPlayer;
		Card selectCard;
		for (int i = 0; i < playerCount - 1; i++) {
			currentPlayer++;
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
				players[startPlayer].hand.remove(select);
			}
			
			// otherwise if hand is 13, can play anything other than hearts
			else if (players[currentPlayer].hand.size() == 13) {
				do {
					select = scanner.nextInt();
					selectCard = players[startPlayer].hand.get(select);
				} while (startCard.getSuit() == Card.Suit.HEARTS);
			}
			
			// if player does not have the same suit he can place anything
			else {
				select = scanner.nextInt();
				selectCard = players[startPlayer].hand.remove(select);
				if (selectCard.getSuit() == Card.Suit.HEARTS || 
					selectCard.compareTo(new Card(12, Card.Suit.SPADES)) == 0 && heartsBroken == false) {
					heartsBroken = true;
				}
			}
			
			pot.put(selectCard, currentPlayer);
			
		}
		
		potAnalyze();
		players[startPlayer].endPile.addAll((Collection<? extends Card>) pot);
		this.pot.clear();
		
	}
	
	public void potAnalyze() {
		Card winCard = null;
		for (Entry<Card, Integer> entry : pot.entrySet()) {
			if (entry.getKey().getSuit() == startSuit) {
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
	}
	
	public void findStartPlayer() {
		for (int j = 0; j < playerCount; j++) {
			for (int i = 0; i < players[j].hand.size(); i++) {
				if (players[j].hand.get(i).compareTo(new Card(2, Card.Suit.CLUBS)) == 0) {
					this.startPlayer = j;
				}
			}
		}
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
	
	public static String printScore(){
		String s = "Scores:";
		s = s + "\n";
		for(HeartsPlayer p: players){
			int i = 1;
			s = s + "Player " + i + ": " + p.getScore(); + "\n";
			i++;
		}
		
		return s;
	}
	
	public static String toString(){
		String s1 = "";
		String s2 = "";
		
		for(Card c: players[0].getHand()){
			String card = Integer.toString(c.getCardNumber()) + c.getSuit();
			s1 = s1 + card + " ";
		}
		
		for(Card c: players[1].getHand()){
			String card = Integer.toString(c.getCardNumber()) + c.getSuit();
			s2 = s2 + card + " ";
		}
		
		s1 = s1.substring(0, s1.length()-1);
		s2 = s1.substring(0, s2.length()-1);
		
		String space = "";
		for(int i = 0; i < 58; i++){
			space = space + " ";
		}
		
		int h1size = s1.length();
		int h2size = s2.length();
		int count = (60 - h1size)/2;
		int count2 = (60 - h2size)/2;
		int count3 = (58 - top.length())/2;
		String dashes1 = "";
		String dashes2 = "";
		
		for(int i = 0; i < count3; i++){
			spaces = spaces + " ";
		}
		
		for(int i = 0; i < count; i++){
			dashes1 = dashes1 + "-";
		}
		
		for(int i = 0; i < count2; i++){
			dashes2 = dashes2 + "-";
		}
		
		s = s + dashes1 + s1 + dashes1 + "\n";
		s = s + "|" + spaces + "|" + "\n";
		s = s + "|" + spaces + "|" + "\n";
		s = s + "|" + spaces + "|" + "\n";
		
		for(int i = 0; i < players[2].getHand().size(); i++){
			List<Card> h1 = players[2].getHand();
			List<Card> h2 = players[3].getHand();
			s = s + h1[i] + spaces + h2[i] + "\n";
		}
		
		s = s + "|" + spaces + "|" + "\n";
		s = s + "|" + spaces + "|" + "\n";
		s = s + "|" + spaces + "|" + "\n";
		s = s + dashes1 + s2 + dashes1 + "\n";
		
		
		ArrayList pKeys = new ArrayList<Card>(pot.keySet());
		ArrayList potp = new ArrayList<Card> ();
		for(Card c: pkeys){
			int num = pot.get(c)
			potp.put(c, num)
		}
		
		s = s + "Pot: "
		for(Card c: potp){
			String card = Integer.toString(c.getCardNumber()) + c.getSuit();
			s = s + card + " "
		}
		
		return s
	}

}