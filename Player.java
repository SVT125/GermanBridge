package cardsuite;

import java.util.*;

public abstract class Player {
	
	protected List<Card> hand;
	protected int score;
	
	public abstract ArrayList<Card> fillHand(ArrayList<Card> deck, Random random);
	public abstract void scoreChange();
	public abstract void organize();
	public abstract boolean hasSuit(Card.Suit suit);
	
}
