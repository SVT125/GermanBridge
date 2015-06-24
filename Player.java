package cardsuite;
import java.util.*;

// UNFINISHED

public abstract class Player {
	
	protected List<Card> hand;
	protected int score;
	
	public abstract void fillHand();
	public abstract void scoreChange();
	public abstract void organize();
	
}
