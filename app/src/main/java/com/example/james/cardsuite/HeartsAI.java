package com.example.james.cardsuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by timothylam on 8/18/15.
 */
public class HeartsAI extends HeartsPlayer {

    boolean attemptShootMoon;

    // Bot chooses the ideal cards to swap at the beginning of each hand round
    public List<Card> chooseSwap(HeartsManager manager, int startPlayer) {
        List<Card> priorityCards = new ArrayList<Card>();

        // if player has queen of spades and has lower spades cards to buffer, keep it; otherwise, toss it
        if (this.hasCard(new Card(12, Card.Suit.SPADES))) {
            if (this.suitCount(Card.Suit.SPADES) < 3 && !attemptShootMoon)
                priorityCards.add(new Card(12, Card.Suit.SPADES));
        }

        List<Card.Suit> prioritySuits = getPrioritySuit();
        for (Card.Suit suit : prioritySuits) {
            for (int i = this.hand.size() - 1; i >= 0; i--) {
                if (hand.get(i).getSuit() == suit)
                    priorityCards.add(hand.get(i));
            }
        }

        List<Card> chosenCards = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            chosenCards.add(priorityCards.get(i));

        return chosenCards;
    }

    // Algorithm determining the corresponding weights of each suit. The higher, the more threatening that suit is
    public int suitWeights(Card.Suit suit) {
        int weight = 0;
        int oneFourCancel = 0;
        int twoThreeCancel = 0;
        for (Card card : hand)
            if (card.getSuit().equals(suit)) {
                switch ((card.getCardNumber() + 1) / 3) {
                    case 1: oneFourCancel -= 4; break;
                    case 2: twoThreeCancel -= 3; break;
                    case 3: twoThreeCancel += 3; break;
                    case 4: oneFourCancel += 4; break;
                    default: weight += 5; break;
                }
            }
        weight = weight + Math.abs(oneFourCancel) + Math.abs(twoThreeCancel);

        return weight;
    }

    public List<Card.Suit> getPrioritySuit() {
        int heartsWeight = suitWeights(Card.Suit.HEARTS);
        int clubsWeight = suitWeights(Card.Suit.CLUBS);
        int diamondsWeight = suitWeights(Card.Suit.DIAMONDS);

        final Map<Card.Suit, Integer> suitWeight = new HashMap<>();
        suitWeight.put(Card.Suit.HEARTS, heartsWeight);
        suitWeight.put(Card.Suit.CLUBS, clubsWeight);
        suitWeight.put(Card.Suit.DIAMONDS, diamondsWeight);
        List<Card.Suit> prioritySuits = new ArrayList(suitWeight.keySet());
        Collections.sort(prioritySuits, new Comparator<Card.Suit>() {
            Map<Card.Suit, Integer> map;

            {
                this.map = suitWeight;
            }

            public int compare(Card.Suit o1, Card.Suit o2) {
                return map.get(o2) - map.get(o1);
            }
        });
        return prioritySuits;
    }

}
