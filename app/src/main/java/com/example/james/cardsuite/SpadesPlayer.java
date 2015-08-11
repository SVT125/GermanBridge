package com.example.james.cardsuite;

import java.util.ArrayList;

public class SpadesPlayer extends Player {
	
	protected int bid, totalBid, bags, obtained, totalObtained;
	protected boolean blindNil = false;
	protected SpadesPlayer partner;
	
	public SpadesPlayer() {
		hand = new ArrayList<Card>();
		this.score = 0;
	}
	
	// PROBABLY VERY ERROR PRONE; BUG FIXES PROBABLY NECESSARY
	public boolean scoreChange() {

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
			}
		}
		if (this.bags >= 10) {
			this.bags -= 10;
			this.score -= 100;
		}

		return false;
	}
}
