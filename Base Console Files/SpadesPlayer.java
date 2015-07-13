package com.example.james.cardsuite;

import java.util.ArrayList;

public class SpadesPlayer extends Player {
	
	protected int bid;
	protected int partnerBid;
	protected int totalBid;
	protected int bags = 0;
	protected int obtained = 0;
	protected int totalObtained = 0;
	
	public SpadesPlayer() {
		hand = new ArrayList<Card>();
		this.score = 0;
	}
	
	// PROBABLY VERY ERROR PRONE; BUG FIXES PROBABLY NECESSARY
	public void scoreChange() {
		totalBid = bid + partnerBid;
		
		// if nil bid is obtained by any person, team gets 100 points
		if (obtained == 0 && (bid == 0 || partnerBid == 0)) {
			score = score + 100;
		}
		
		if (totalBid >= totalObtained) {
			// score if guessed right increases by ten times the total bid plus one time each obtained over
			score = score + totalBid * 10 + (totalBid - totalObtained);
			// bags increase for each one over that the team guesses
			bags = bags + (totalBid - totalObtained);
			// if bags reach ten, score deducted by 100
			if (bags >= 10) {
				bags = bags - 10;
				score = score - 100;
			}
		}
		// otherwise, team loses 10 points for each miss
		else {
			score = score - (totalBid - totalObtained) * 10;
		}
	}
	
}
