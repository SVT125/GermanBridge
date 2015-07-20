package cardsuite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class SpadesManager extends Manager {

	private boolean spadesBroken = false;
	private SpadesPlayer[] players;
	private List<SpadesPlayer> playersOneThree;
	private List<SpadesPlayer> playersTwoFour;
	
	public SpadesManager() {
		
		playerCount = 4;
		players = new SpadesPlayer[playerCount];
		playersOneThree = new ArrayList<SpadesPlayer>();
		playersTwoFour = new ArrayList<SpadesPlayer>();
		startPlayer = 0;
		
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
		
		playersOneThree.add(players[0]);
		playersOneThree.add(players[2]);
		playersTwoFour.add(players[1]);
		playersTwoFour.add(players[3]);
		
	}
	
	public static void main(String args[]) {
		
		SpadesManager manager=  new SpadesManager();
		System.out.println(manager);
		
		manager.bid();
		
		for (int i = 0; i < manager.roundCount; i++) {
			manager.potHandle();
		}
		
		for (SpadesPlayer player : manager.players) {
			player.scoreChange();
		}
		
		manager.printScore();
		
		manager.reset();
		
	}
	
	public void reset() {
		roundCount++;
		spadesBroken = false;
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
		}
	}
	
	public void potHandle() {
		
		Card startCard;
		int chosen;
		System.out.println("Player " + Integer.toString(startPlayer + 1) + ": place card pls");
		
		// first player move; cannot place spades
		do {
			chosen = scanner.nextInt();
		} while (players[startPlayer].hand.get(chosen).getSuit() == Card.Suit.SPADES);
		
		startCard = players[startPlayer].hand.remove(chosen);
		int currentPlayer = startPlayer;
		pot.put(startCard, startPlayer);
		startSuit = startCard.getSuit();
		
		System.out.println(this);
		System.out.println(potString());
		
		for (int i = 0; i < 3; i++) {
			currentPlayer++;
			if (currentPlayer == playerCount) {
				currentPlayer = 0;
			}
			
			System.out.println("Player " + Integer.toString(currentPlayer + 1) + ": place card pls");
			
			// must place down a card of the same suit
			if (players[currentPlayer].hasSuit(startSuit)) {
				do {
					chosen = scanner.nextInt();
				} while (!(players[startPlayer].hand.get(chosen).getSuit().equals(startSuit)));
			}
			
			// if first turn and has none of same card, cannot place spades
			else if (players[currentPlayer].hand.size() == 13) {
				do {
					chosen = scanner.nextInt();
				} while (players[startPlayer].hand.get(chosen).getSuit().equals(Card.Suit.SPADES));
			}
			
			// otherwise can place anything
			else {
				chosen = scanner.nextInt();
				if (players[startPlayer].hand.get(chosen).getSuit().equals(Card.Suit.SPADES) && !spadesBroken) {
					spadesBroken = true;
				}
			}
			
			pot.put(players[currentPlayer].hand.remove(chosen), currentPlayer);
			System.out.println(this);
			System.out.println(potString());
		}
		
		potAnalyze();
		
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
			// if spades exists, it tops normal cards
			else if (entry.getKey().getSuit().equals(Card.Suit.SPADES)) {
				if (winCard == null || winCard.getSuit() != Card.Suit.SPADES) {
					winCard = entry.getKey();
					startPlayer = entry.getValue();
				}
				else if (entry.getKey().getCardNumber() > winCard.getCardNumber()){
					winCard = entry.getKey();
					startPlayer = entry.getValue();
				}
			}
		}
		players[startPlayer].obtained++;
		// CONDENSE SOMEHOW PLS
		if (startPlayer == 1 || startPlayer == 3) {
			for (SpadesPlayer player : playersOneThree) {
				player.totalObtained++;
			}
		}
		else {
			for (SpadesPlayer player : playersTwoFour) {
				player.totalObtained++;
			}
		}
		System.out.println("Player " + Integer.toString(startPlayer + 1) + " wins!");
	}
	
	public void bid() {
		
		int bid;
		int currentPlayer = startPlayer;
		
		for (int i = 0; i < playerCount; i++) {
			
			if (currentPlayer == playerCount) {
				currentPlayer = 0;
			}
			System.out.println("Player " + Integer.toString(currentPlayer + 1) + " place bid pls:");
			do {
				bid = scanner.nextInt();
			} while (bid < -1 || bid > 14);
			
			players[currentPlayer].bid = bid;
			currentPlayer++;
		}
		
		// total bids added up for partners.. neaten up this code please
		players[0].partnerBid = players[2].bid;
		players[1].partnerBid = players[3].bid;
		players[2].partnerBid = players[0].bid;
		players[3].partnerBid = players[1].bid;
		
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
