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

    private int gameMode = 0;
    private int difficulty = 0;

    public HeartsAI(int difficulty) {
        super();
        this.isBot = true;
        this.difficulty = difficulty;
        this.findGameMode();
    }

    // Bot chooses the ideal cards to swap at the beginning of each hand round
    public List<Card> chooseSwap() {
        this.organize();
        List<Card> priorityCards = new ArrayList<Card>();

        // if player has queen of spades and has lower spades cards to buffer, keep it; otherwise, toss it
        if (this.hasCard(12, Card.Suit.SPADES)) {
            if (this.suitCount(Card.Suit.SPADES) < 3 && (gameMode != 4))
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

    // Takes suit weights and orders them and returns as a list from highest priority to lowest
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
        if (currentPlayer == startPlayer)
            return this.leadMove(manager);
        else if (currentPlayer == (startPlayer + 3) % 4)
            return this.lastMove(manager);
        else
            return this.move(manager);
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
        else if (this.hasCard(12, Card.Suit.SPADES))
            return (new Card(12, Card.Suit.SPADES));
        else if (manager.isInPlayersHands(12, Card.Suit.SPADES)) {
            if (this.hasCard(14, Card.Suit.SPADES))
                return (new Card(14, Card.Suit.SPADES));
            else if (this.hasCard(13, Card.Suit.SPADES))
                return (new Card(13, Card.Suit.SPADES));
        }

        // else play highest of suit that is the most dangerous (weights)
        List<Card.Suit> priorities = getPrioritySuit();
        Card.Suit prioritySuit = priorities.get(0);
        return playHighestSuit(prioritySuit);
    }

    // Selects a move when bot is the one leading the current pot
    public Card leadMove(HeartsManager manager) {

        if (this.hand.size() == 13)
            return new Card(2, Card.Suit.CLUBS);

        // if person does not have queen of spades and has a safe hand for spades, place low spades
        if (!(this.hasCard(12, Card.Suit.SPADES)) && (suitWeights(Card.Suit.SPADES) < 3) && this.hasSuit(Card.Suit.SPADES)) {
            return this.playLowestSuit(Card.Suit.SPADES);
        }

        if (manager.heartsBroken) {
            // otherwise lead in suit that has been played the least
            Card.Suit leastSuit = manager.leastPlayedSuit(true);
            return this.playLowestSuit(leastSuit);
        }
        else {
            Card.Suit leastSuit = manager.leastPlayedSuit(false);
            return this.playLowestSuit(leastSuit);
        }
    }

    // Selects a move when bot is neither last nor first player
    public Card move(HeartsManager manager) {
        Card.Suit playSuit = manager.startSuit;

        // if bot has the suit, return the lowest of that suit
        if (this.hasSuit(playSuit))
            return playLowestSuit(playSuit);

        // else play highest of suit that is the most dangerous (weights)
        List<Card.Suit> priorities = getPrioritySuit();
        Card.Suit prioritySuit = priorities.get(0);
        return playHighestSuit(prioritySuit);
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

    public void removeCard(Card card) {
        for (Card card1 : hand) {
            if (card1.compareTo(card) == 0) {
                hand.remove(card1);
                return;
            }
        }
    }

    // Dynamically changes the bots' play style during the game depending on the circumstances
    // 1 - Low Layer , 2 - Voider, 3 - Equalizer, 4 - Moon Shooter
    // TODO
    public void findGameMode() {
        this.gameMode = 1;
    }

}
