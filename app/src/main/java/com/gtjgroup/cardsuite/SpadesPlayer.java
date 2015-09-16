package com.gtjgroup.cardsuite;

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

    public boolean scoreChange() {
        int previousBags = bags;

        // if person gets 0 hands

        if (totalBid > totalObtained) {
            this.score -= (10 * totalBid);
        } else {
            this.score += (10 * totalBid);
            this.bags += (totalObtained - totalBid);
            this.score += (bags - previousBags);
        }

        if (this.bags >= 10) {
            this.bags -= 10;
            this.score -= 100;
        }

        totalObtained = 0;
        obtained = 0;
        handsWon = 0;
        bid = 0;
        totalBid = 0;
        scoreHistory.add(this.score);

        return false;
    }
}
