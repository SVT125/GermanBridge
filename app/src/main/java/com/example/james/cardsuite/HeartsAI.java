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
        this.organize();
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
        int low1 = 0, low2 = 0, high1 = 0, high2 = 0;
        for (Card card : hand)
            if (card.getSuit().equals(suit)) {
                switch ((card.getCardNumber() + 1) / 3) {
                    case 1: low1++; break;
                    case 2: low2++; break;
                    case 3: high1++; break;
                    case 4: high2++; break;
                    default: weight += 2; break;
                }
            }
        weight = weight + (high2 - low1) * 2 + (high1 - low2);

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

    // Selects a move for the bot at any point in the game
    public Card makeMove(int currentPlayer, int startPlayer, HeartsManager manager) {
        if (currentPlayer == startPlayer) {
            return this.leadMove();
        }
        else if (currentPlayer == (startPlayer + 3) % 4) {
            return this.lastMove(manager);
        }
        else {
            return this.move(manager);
        }
    }

    // Selects a move when bot is the one ending the current pot
    public Card lastMove(HeartsManager manager) {
        Card.Suit playSuit = manager.startSuit;

        // if player has suit and pot does not have Q spades or hearts, safely play highest card of suit
        if (this.hasSuit(playSuit) && !(manager.potHasSuit(Card.Suit.HEARTS)) && manager.pot.containsValue(new Card(12, Card.Suit.SPADES)))
            return this.playHighestSuit(playSuit);

        // if pot contains such values of Q of spades and hearts,
        else if (this.hasSuit(playSuit) && this.playLowestSuit(playSuit).getCardNumber() > manager.potHighestValue())
            return this.playHighestSuit(playSuit);

        // if player has suit and the pot contains point giving cards, play
        else if (this.hasSuit(playSuit))
            return this.playLowestSuit(playSuit);

        // otherwise dump queen of spades, other high spades, and any other hearts
        else if (this.hasCard(new Card(12, Card.Suit.SPADES)))
            return (new Card(12, Card.Suit.SPADES));
        /*else if (!(manager.hasBeenPlayed(new Card(12, Card.Suit.SPADES)))) {
            if (this.hasCard(new Card(14, Card.Suit.SPADES)))
                return (new Card(14, Card.Suit.SPADES));
            else if (this.hasCard(new Card(13, Card.Suit.SPADES)))
                return (new Card(13, Card.Suit.SPADES));
        }*/
        else if (this.hasSuit(Card.Suit.HEARTS))
            return this.playHighestSuit(Card.Suit.HEARTS);
        else if (this.hasSuit(Card.Suit.CLUBS))
            return this.playHighestSuit(Card.Suit.CLUBS);
        else if (this.hasSuit(Card.Suit.DIAMONDS))
            return this.playHighestSuit(Card.Suit.DIAMONDS);
        else
            return this.playHighestSuit(Card.Suit.SPADES);
    }

    // Selects a move when bot is the one leading the current pot
    // TODO
    public Card leadMove() {

        // if person does not have queen of spades and has a safe hand for spades, place low spades
        if (!(this.hasCard(new Card(12, Card.Suit.SPADES))) && (suitWeights(Card.Suit.SPADES) < 3) && this.hasSuit(Card.Suit.SPADES)) {
            return this.playLowestSuit(Card.Suit.SPADES);
        }

        return null;
    }

    // Selects a move when bot is neither last nor first player
    // TODO
    public Card move(HeartsManager manager) {
        return null;
    }

    public Card playLowestSuit(Card.Suit suit) {
        this.organize();
        for (Card card : this.hand)
            if (card.getSuit().equals(suit))
                return card;
        return null;
    }

    public Card playHighestSuit(Card.Suit suit) {
        this.organize();
        for (int i = this.hand.size() - 1; i >= 0; i--)
            if (this.hand.get(i).getSuit().equals(suit))
                return this.hand.get(i);
        return null;
    }

}
