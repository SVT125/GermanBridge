package com.example.james.cardsuite;

import android.os.Parcelable;

import android.os.Parcel;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class BridgeManager extends Manager implements Parcelable {

	public int addedGuesses = 0;
	public Card.Suit trumpSuit;
	public Card trumpCard; //Variable included in case want to display the trump card

	public BridgeManager(int player) {
		pot = new LinkedHashMap<Integer, Card>();
		players = new BridgePlayer[playerCount];
		startPlayer = player;

		// make deck
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}

		// give players cards via fillHand method
		for (int i = 0; i < playerCount; i++) {
			players[i] = new BridgePlayer();
			deck = players[i].fillHand(deck, random, potsFinished);
			players[i].handsWon = 0;
		}

		// find a trumpCard
		trumpCard = deck.remove(random.nextInt(deck.size()));
		trumpSuit = trumpCard.getSuit();
	}

	//Resets the state of the manager object for the next round.
	public void reset() {
		this.addedGuesses = 0;
		deck.clear();
		potsFinished++;
		pot = new LinkedHashMap<Integer, Card>();
		for (int i = 2; i < 15; i++) {
			for (Card.Suit suits : Card.Suit.values()) {
				deck.add(new Card(i, suits));
			}
		}
		for (int i = 0; i < playerCount; i++) {
			deck = players[i].fillHand(deck, random, potsFinished);
			players[i].handsWon = 0;
		}
		trumpCard = deck.remove(random.nextInt(deck.size()));
		trumpSuit = trumpCard.getSuit();
	}

	public void potHandle(int chosen, int currentPlayer) {
		// other players choose cards - must place similar suit to startSuit if they have it.
		pot.put(currentPlayer, players[currentPlayer].hand.get(chosen));

		if(currentPlayer == startPlayer) {
			// first player can choose any card he/she wants
			startSuit = players[startPlayer].hand.remove(chosen).getSuit();
			return;
		}

		players[currentPlayer].hand.remove(chosen);
	}

	//Returns true if the given card should be selectable to the player
	public boolean cardSelectable(Card card, boolean finishedSwapping, int currentPlayer) {
		if (players[currentPlayer].hasSuit(startSuit) && card.getSuit() == startSuit)
			return true;
		else if (!(players[currentPlayer].hasSuit(startSuit)))
			return true;
		return false;
	}

	//Analyzes the pot and updates the pile count for the winning player of the pot.
	public void potAnalyze() {
		Card winCard = null;
		for (Entry<Integer, Card> entry : pot.entrySet()) {
			if (entry.getValue().getSuit() == startSuit) {
				if (winCard == null || entry.getValue().getCardNumber() > winCard.getCardNumber()) {
					winCard = entry.getValue();
					startPlayer = entry.getKey();
				}
			}
			// if a trump card exists it wins/gets compared to other trump cards
			else if (entry.getValue().getSuit() == trumpSuit) {
				if ((winCard == null || winCard.getSuit() != trumpSuit) || entry.getValue().getCardNumber() > winCard.getCardNumber()) {
					winCard = entry.getValue();
					startPlayer = entry.getKey();
				}
			}
		}
		// whoever wins pile gets one towards their obtained pile count
		((BridgePlayer)players[startPlayer]).obtained++;
		((BridgePlayer)players[startPlayer]).handsWon++;
	}

	public List<Card> chooseCards(int playerNum, List<Integer> chosenIndices) {return null;};
	public void swapCards(Collection<?> chosen, int playerNum, int swapRound) {return;};

	public Player[] getPlayers() { return this.players; }

	public int findStartPlayer() {
		return this.startPlayer;
	}

	public int findWinner() {
		int max = Integer.MIN_VALUE, winner = -1;
		for (int i = 0; i < 4; i++) {
			if (players[i].score > max) {
				winner = i;
				max = players[i].score;
			}
		}
		return winner;
	}

	public boolean isGameOver() {
		if (totalRoundCount == (52/playerCount)) return true;
		return false;
	}


	// METHODS THAT MAKE BRIDGEMANAGER PARCELABLE
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(pot.size());
		for (Entry<Integer, Card> entry : this.pot.entrySet()) {
			out.writeInt(entry.getKey());
			out.writeInt(entry.getValue().getCardNumber());
			out.writeSerializable(entry.getValue().getSuit());
		}
		out.writeSerializable(this.startSuit);
		out.writeInt(startPlayer);
		out.writeInt(totalRoundCount);
		out.writeInt(potsFinished);
		out.writeInt(addedGuesses);
		out.writeInt(usedCards.size());
		for (Card card : usedCards) {
			out.writeInt(card.getCardNumber());
			out.writeSerializable(card.getSuit());
		}
		out.writeSerializable(trumpSuit);
		out.writeInt(trumpCard.getCardNumber());
		for (Player player : players) {
			out.writeParcelable((BridgePlayer)player, flags);
		}
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public BridgeManager createFromParcel(Parcel in) { return new BridgeManager(in); }
		public BridgeManager[] newArray(int size) { return new BridgeManager[size]; }
	};

	private BridgeManager(Parcel in) {
		int size = in.readInt();
		this.pot = new LinkedHashMap<Integer, Card>();
		for (int i = 0; i < size; i++) {
			int player = in.readInt();
			int cardNum = in.readInt();
			Card.Suit cardSuit = (Card.Suit) in.readSerializable();
			this.pot.put(player, new Card(cardNum, cardSuit));
		}
		startSuit = (Card.Suit) in.readSerializable();
		startPlayer = in.readInt();
		totalRoundCount = in.readInt();
		potsFinished = in.readInt();
		addedGuesses = in.readInt();
		int size2 = in.readInt();
		for (int i = 0; i < size2; i++) {
			int cardNum = in.readInt();
			Card.Suit cardSuit = (Card.Suit) in.readSerializable();
			usedCards.add(new Card(cardNum, cardSuit));
		}
		trumpSuit = (Card.Suit) in.readSerializable();
		trumpCard = new Card(in.readInt(), trumpSuit);
		players = new BridgePlayer[playerCount];
		for (int i = 0; i < 4; i++) {
			players[i] = in.readParcelable(getClass().getClassLoader());
		}
	}

	public int describeContents() { return 0; }

}
