package com.example.james.cardsuite;

import java.io.Serializable;
import java.util.ArrayList;

public class BridgePlayer extends Player implements Serializable {
	
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
		guess = 0;
		obtained = 0;
		return false;
	}
}
