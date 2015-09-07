package com.example.james.cardsuite;

import java.io.Serializable;
import java.util.ArrayList;

public class SpadesPlayer extends Player implements Serializable {
	
	protected int bid, totalBid, bags, obtained, totalObtained;
	protected boolean blindNil = false;
	protected SpadesPlayer partner;
	
	public SpadesPlayer() {
		hand = new ArrayList<Card>();
		this.score = 0;
	}
	
	// PROBABLY VERY ERROR PRONE; BUG FIXES PROBABLY NECESSARY
	public boolean scoreChange() {

		System.out.println(bid);
		System.out.println(totalBid);
		System.out.println(bags);
		System.out.println(obtained);
		System.out.println(totalObtained);

		int previousBags = bags;

		// if person gets 0 hands
		if (obtained == 0) {
			if (blindNil) { this.score += 200; partner.score += 200; }
			else if (bid == 0) { this.score += 100; partner.score += 100; }
		}
		else {
			if (totalBid > totalObtained) { this.score -= (10 * totalBid); }
			else {
				this.score += (10 * totalBid);
				this.bags += (totalObtained - totalBid);
				this.score += (bags - previousBags);
			}
		}
		if (this.bags >= 10) {
			this.bags -= 10;
			this.score -= 100;
		}

		totalObtained = 0;
		obtained = 0;
		handsWon = 0;
		scoreHistory.add(this.score);

		return false;
	}
}
