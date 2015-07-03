package cardsuite;

import java.util.ArrayList;

public class SpadesPlayer extends Player {
	
	protected int bid;
	protected int partnerBid;
	protected int totalBid;
	protected int bags = 0;
	protected int bagLimit = 10;
	protected int obtained = 0;
	
	public SpadesPlayer() {
		hand = new ArrayList<Card>();
		this.score = 0;
	}
	
	public void scoreChange() {
		
	}
	
}
