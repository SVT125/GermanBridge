package com.gtjgroup.cardsuite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by timothylam on 8/18/15.
 */
public class HeartsAI extends HeartsPlayer implements Serializable {

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
        for (Card.Suit suit : prioritySuits)
            for (int i = this.hand.size() - 1; i >= 0; i--)
                if (hand.get(i).getSuit() == suit && (!suit.equals(Card.Suit.SPADES)))
                    priorityCards.add(hand.get(i));

        if (priorityCards.size() < 3) {
            for (int i = this.hand.size() - 1; i >= 0; i--)
                if (hand.get(i).getSuit().equals(Card.Suit.SPADES))
                    priorityCards.add(hand.get(i));
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
        if (weight > 0) { weight = weight * 2; }
        if (weight < 0) { weight = Math.abs(weight); }

        return weight;
    }

    // Takes suit weights and orders them and returns as a list from highest priority to lowest
    public List<Card.Suit> getPrioritySuit() {
        int heartsWeight = suitWeights(Card.Suit.HEARTS);
        int clubsWeight = suitWeights(Card.Suit.CLUBS);
        int diamondsWeight = suitWeights(Card.Suit.DIAMONDS);
        int spadesWeight = suitWeights(Card.Suit.SPADES);

        final Map<Card.Suit, Integer> suitWeight = new HashMap<>();
        suitWeight.put(Card.Suit.HEARTS, heartsWeight);
        suitWeight.put(Card.Suit.CLUBS, clubsWeight);
        suitWeight.put(Card.Suit.DIAMONDS, diamondsWeight);
        suitWeight.put(Card.Suit.SPADES, spadesWeight);
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
        for (Card.Suit suit : suitWeight.keySet()) {
            if (suitWeight.get(suit) == 0) {
                prioritySuits.remove(suit);
            }
        }
        return prioritySuits;
    }

    // Selects a move for the bot at any point in the game
    public Card makeMove(int currentPlayer, int startPlayer, HeartsManager manager) {
        if (currentPlayer == startPlayer)
            return this.leadMove(manager);
        else if (currentPlayer == (startPlayer + 3) % 4)
            return this.lastMove(manager);
        else
            return this.move(manager, currentPlayer);
    }

    public Card lastMove(HeartsManager manager) {
        this.organize();
        Card.Suit playSuit = manager.startSuit;
        List<Card> placeable = findPlaceable(manager, (manager.startPlayer + 3) % 4);

        // if player has suit and pot does not have Q spades or hearts, safely play highest card of suit
        if (this.hasSuit(placeable, playSuit) && !(manager.potHasSuit(Card.Suit.HEARTS)) && !(manager.pot.containsValue(new Card(12, Card.Suit.SPADES))))
            return placeable.get(placeable.size() - 1);

        else if (this.hasSuit(placeable, playSuit) && (placeable.get(0).getCardNumber() > manager.potHighestValue()))
            return placeable.get(placeable.size() - 1);

        else if (this.hasSuit(placeable, playSuit))
            return this.playLowestSuit(placeable, manager.potHighestValue());

        else if (this.hasCard(12, Card.Suit.SPADES) && hand.size() != 13)
            return (new Card(12, Card.Suit.SPADES));

        else if (manager.isInPlayersHands(12, Card.Suit.SPADES)) {
            if (this.hasCard(14, Card.Suit.SPADES))
                return (new Card(14, Card.Suit.SPADES));
            else if (this.hasCard(13, Card.Suit.SPADES))
                return (new Card(13, Card.Suit.SPADES));
        }

        List<Card.Suit> priorities = getPrioritySuit();
        if (!(priorities.isEmpty())) {
            Card.Suit prioritySuit = priorities.get(0);
            return playHighestSuit(prioritySuit);
        }
        return hand.get(0);
    }

    // Selects a move when bot is the one leading the current pot
    public Card leadMove(HeartsManager manager) {
        this.organize();
        List<Card> placeable = findPlaceable(manager, manager.startPlayer);

        if (this.hand.size() == 13)
            return (new Card(2, Card.Suit.CLUBS));

        // if person does not have queen of spades and has a safe hand for spades, place low spades
        if (!(this.hasCard(12, Card.Suit.SPADES)) && (suitWeights(Card.Suit.SPADES) < 3) && this.hasSuit(placeable, Card.Suit.SPADES))
            return this.playLowestSuit(Card.Suit.SPADES, placeable);

        if (manager.heartsBroken) {
            // otherwise lead in suit that has been played the least
            Card.Suit leastSuit = manager.leastPlayedSuit(true);
            if (this.hasSuit(placeable, leastSuit)) {
                return this.playLowestSuit(leastSuit, placeable);
            }
        }
        else {
            Card.Suit leastSuit = manager.leastPlayedSuit(false);
            if (this.hasSuit(placeable, leastSuit)) {
                return this.playLowestSuit(leastSuit, placeable);
            }
        }
        return placeable.get(0);
    }

    // Selects a move when bot is neither last nor first player
    public Card move(HeartsManager manager, int currentPlayer) {
        this.organize();
        Card.Suit playSuit = manager.startSuit;
        List<Card> placeable = findPlaceable(manager, currentPlayer);

        if (this.hand.size() == 13 && placeable.size() == 0)
            return manager.getPlayers()[currentPlayer].hand.get(0);
        else if (this.hand.size() == 13)
            return placeable.get(placeable.size() - 1);

        // if bot has the suit, return the lowest of that suit
        if (this.hasSuit(placeable, playSuit) && (placeable.get(0).getCardNumber() > manager.potHighestValue()))
            return playHighestSuit(playSuit);

        else if (this.hasSuit(placeable, playSuit))
            return playLowestSuit(placeable, manager.potHighestValue());

        else if (this.hasCard(12, Card.Suit.SPADES) && hand.size() != 13)
            return (new Card(12, Card.Suit.SPADES));

        else if (manager.isInPlayersHands(12, Card.Suit.SPADES)) {
            if (this.hasCard(14, Card.Suit.SPADES))
                return (new Card(14, Card.Suit.SPADES));
            else if (this.hasCard(13, Card.Suit.SPADES))
                return (new Card(13, Card.Suit.SPADES));
        }

        // else play highest of suit that is the most dangerous (weights)
        List<Card.Suit> priorities = getPrioritySuit();
        if (!priorities.isEmpty()) {
            Card.Suit prioritySuit = priorities.get(0);
            return playHighestSuit(prioritySuit);
        }
        return hand.get(hand.size() - 1);
    }

    public boolean hasSuit(List<Card> placeable, Card.Suit suit) {
        if (placeable.get(0).getSuit().equals(suit))
            return true;
        return false;
    }

    public Card playLowestSuit(Card.Suit suit, List<Card> placeable) {
        for (int i = 0; i < placeable.size(); i++) {
            if (placeable.get(i).getSuit().equals(suit)) {
                return placeable.get(i);
            }
        }
        return null;
    }

    public Card playLowestSuit(List<Card> placeable, int minNumPot) {
        for (int i = placeable.size() - 1; i >= 0; i--) {
            if (placeable.get(i).getCardNumber() < minNumPot) {
                return placeable.get(i);
            }
        }
        return null;
    }

    public Card playLowestSuit(List<Card> placeable, Card.Suit suit, int minNumPot) {
        for (int i = placeable.size() - 1; i >= 0; i--) {
            if (placeable.get(i).getSuit().equals(suit) && placeable.get(i).getCardNumber() < minNumPot) {
                return placeable.get(i);
            }
        }
        return null;
    }

    public Card playHighestSuit(Card.Suit suit) {
        this.organize();
        for (int i = this.hand.size() - 1; i >= 0; i--) {
            if (this.hand.get(i).getSuit().equals(suit))
                return this.hand.get(i);
        }
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

    public List<Card> findPlaceable(HeartsManager manager, int currentPlayer) {
        List<Card> placeable = new ArrayList<>();
        for (Card card : this.hand)
            if (manager.cardSelectable(card, true, currentPlayer))
                placeable.add(card);
        return placeable;
    }

    // Dynamically changes the bots' play style during the game depending on the circumstances
    // 1 - Low Layer , 2 - Voider, 3 - Equalizer, 4 - Moon Shooter
    // TODO
    public void findGameMode() {
        this.gameMode = 1;
    }

}
