package cardsuite;
import java.util.*;

// UNFINISHED

public abstract class Player {
	
	protected List<Card> hand;
	protected int score;
	
	public abstract ArrayList<Card> fillHand(ArrayList<Card> deck, Random random);
	public abstract void scoreChange();
	public abstract void organize();
	
}
