package com.example.james.cardsuite;

import java.util.ArrayList;
import java.util.Random;

public class BridgePlayer extends Player {
	
	protected int guess;
	protected int obtained;

	public BridgePlayer() {
		hand = new ArrayList<Card>();
		this.score = 0;
	}
	
	public void scoreChange() {
		if (guess == obtained) {
			score = score + 10 + (int)Math.pow(2, obtained);
		}
		else {
			int difference = Math.abs(guess - obtained);
			score = score - (int)Math.pow(2, difference);
		}
		guess = 0;
		obtained = 0;
	}
	
}
