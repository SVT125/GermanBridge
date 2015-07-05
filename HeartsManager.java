package cardsuite;
import java.util.*;
import java.util.Map.Entry;

public class HeartsManager extends Manager {
	
	private boolean heartsBroken = false;
	
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

	//Resets the state of the manager object for the next round.
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

	//Called to handle every player's chosen card in the pot and analyzes the pot once all cards are put.
	//This method is only used for console testing, won't apply to the Android app - don't input out of bounds.
	public void potHandle() {
		int select;
		Card startCard;
		
		// first move by first player
		if (players[startPlayer].hand.size() == 13) {
			//Have the start player select the 2 of clubs, keep prompting for selection until they do.
			do {
				System.out.println("Select the card to put in the pot, indexes 0 - " + players[startPlayer].hand.size()-1 + ": " );
				select = scanner.nextInt();
			} while (players[startPlayer].hand.get(select).compareTo(new Card(2, Card.Suit.CLUBS)) != 0);

			startCard = players[startPlayer].hand.remove(select);
		}
		else {
			// if hearts is not broken, card cannot be hearts or queen of spades
			if (heartsBroken) {
				System.out.println("Select the card to put in the pot, indexes 0 - " + players[startPlayer].hand.size()-1 + ": " );
				select = scanner.nextInt();
				startCard = players[startPlayer].hand.remove(select);
			}
			else {
				do {
					System.out.println("Select the card to put in the pot, indexes 0 - " + players[startPlayer].hand.size()-1 + ": " );
					select = scanner.nextInt();
					startCard = players[startPlayer].hand.get(select);
				} while (startCard.getSuit() == Card.Suit.HEARTS || startCard.compareTo(new Card(12,Card.Suit.SPADES)) == 0);
				players[startPlayer].hand.remove(select);
			}
		}
		//Remove the chosen card and put it in the pot
		pot.put(startCard, startPlayer);
		startSuit = startCard.getSuit();
		
		// after first move, other three players place their cards down
		int currentPlayer = startPlayer;
		Card selectCard;
		for (int i = 0; i < playerCount - 1; i++) {
			currentPlayer = (currentPlayer+1) % 4;
			
			// if player has a card of the same suit
			if (players[currentPlayer].hasSuit(startSuit)) {
				do {
					System.out.println("Select the card to put in the pot, indexes 0 - " + players[startPlayer].hand.size()-1 + ": " );
					select = scanner.nextInt();
					selectCard = players[startPlayer].hand.get(select);
				} while (startCard.getSuit() != startSuit);
				players[startPlayer].hand.remove(select);
			}

			// otherwise if hand is 13, can play anything other than hearts
			else if (players[currentPlayer].hand.size() == 13) {
				do {
					System.out.println("Select the card to put in the pot, indexes 0 - " + players[startPlayer].hand.size()-1 + ": " );
					select = scanner.nextInt();
					selectCard = players[startPlayer].hand.get(select);
				} while (startCard.getSuit() == Card.Suit.HEARTS);
			}
			
			// if player does not have the same suit he can place anything
			else {
				System.out.println("Select the card to put in the pot, indexes 0 - " + players[startPlayer].hand.size()-1 + ": " );
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

	//Analyzes the pot and determines the winning card and start player for the next round.
	public void potAnalyze() {
		Card winCard = null;
		for (Entry<Card, Integer> entry : pot.entrySet()) {
			if (entry.getKey().getSuit() == startSuit) {
				if (winCard == null || entry.getKey().getCardNumber() > winCard.getCardNumber()) {
					winCard = entry.getKey();
					startPlayer = entry.getValue();
				}
			}
		}
	}

	//Determines the start player for the first round.
	public void findStartPlayer() {
		for (int j = 0; j < playerCount; j++) {
			for (int i = 0; i < players[j].hand.size(); i++) {
				if (players[j].hand.get(i).compareTo(new Card(2, Card.Suit.CLUBS)) == 0) {
					this.startPlayer = j;
				}
			}
		}
	}

	//Called for the swapping part of the round, alongside swapCards - returns the cards chosen of player index playerNum.
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

	//Called for the swapping part of the round, alongside swapCards - swaps the given cards for players.
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

		// find player with 2 of clubs - we do this here because the card may be swapped.
		manager.findStartPlayer();

		// handles the pot stuff
		for (int i = 0; i < 13; i++) {
			manager.potHandle();
		}

		for (HeartsPlayer player : manager.players) {
			player.scoreChange();
		}

		// reshuffles deck and increments round count for next round
		manager.reset();

	}
}
