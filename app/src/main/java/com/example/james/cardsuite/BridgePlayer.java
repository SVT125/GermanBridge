package com.example.james.cardsuite;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class BridgePlayer extends Player implements Parcelable {
	
	protected int guess;
	protected int obtained;

	public BridgePlayer() {
		hand = new ArrayList<Card>();
		this.score = 0;
	}
	
	public boolean scoreChange() {
		if (guess == obtained) {
			if (guess == 0) score += 10;
			else score = score + 10 + (int)Math.pow(2, obtained);
		}
		else {
			int difference = Math.abs(guess - obtained);
			score = score - (int)Math.pow(2, difference);
		}
		scoreHistory.add(this.score);
		guess = 0;
		obtained = 0;
		return false;
	}

	// METHODS THAT MAKE BRIDGEPLAYER PARCELABLE
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(this.hand.size());
		for (Card card : hand) {
			out.writeInt(card.getCardNumber());
			out.writeSerializable(card.getSuit());
		}
		out.writeInt(score);
		out.writeInt(handsWon);
		out.writeByte((byte) (isBot ? 1 : 0));
		out.writeInt(scoreHistory.size());
		for (Integer integer : scoreHistory) {
			out.writeInt(integer);
		}
		out.writeInt(guess);
		out.writeInt(obtained);
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public BridgePlayer createFromParcel(Parcel in) { return new BridgePlayer(in); }
		public BridgePlayer[] newArray(int size) { return new BridgePlayer[size]; }
	};

	private BridgePlayer(Parcel in) {
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			int cardNum = in.readInt();
			Card.Suit cardSuit = (Card.Suit) in.readSerializable();
			this.hand.add(new Card(cardNum, cardSuit));
		}
		score = in.readInt();
		handsWon = in.readInt();
		isBot = in.readByte() != 0;
		int scoreSize = in.readInt();
		for (int i = 0; i < scoreSize; i++) {
			scoreHistory.add(in.readInt());
		}
		this.guess = in.readInt();
		this.obtained = in.readInt();
	}

	public int describeContents() { return 0; }
}
