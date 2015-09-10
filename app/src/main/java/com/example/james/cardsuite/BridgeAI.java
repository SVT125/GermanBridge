package com.example.james.cardsuite;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// The German Bridge AI. Heuristic eval function sourced mostly from http://www.fongboy.com/programs/Whist/whist_paper.pdf with Luckhardt/Irani's MaxN algorithm.
// Will also consider http://www.cs.umd.edu/~bswilson/presentation.pdf for socially oriented evaluations and varying difficulties.
public class BridgeAI {
    // Returns the suggested number of bids for the given player.
    // Calculated by finding the number of cards of the trump suit and subtracting by a random number [0-3].
    public static int getBid(int currentPlayer, BridgeManager manager) {
        BridgePlayer player = (BridgePlayer)manager.getPlayers()[currentPlayer];
        if (manager.potsFinished + 1 == 1)
            return turnOneBid(currentPlayer, player, manager);
        if (manager.potsFinished + 1 < 5)
            return turnTwoToFourBid(currentPlayer, player, manager);
        if (manager.potsFinished + 1 < 9)
            return turnFiveToEightBid(currentPlayer, player, manager);
        return turnNineToTwelveBid(player, manager, currentPlayer);
    }

    public static int turnOneBid(int currentPlayer, BridgePlayer player, BridgeManager manager) {
        Card c = player.hand.get(0);
        if (c.getSuit().equals(manager.trumpSuit))
            return 1;
        else if (currentPlayer == manager.startPlayer)
            if (c.getCardNumber() > 10)
                return 1;
        else if (currentPlayer == (manager.startPlayer + 3) % 4)
            if (manager.addedGuesses > 0)
                return 1;
        return 0;
    }

    public static int turnTwoToFourBid(int currentPlayer, BridgePlayer player, BridgeManager manager) {
        double bid = 0;
        for (Card c : player.hand) {
            if (c.getSuit().equals(manager.trumpSuit))
                bid++;
            else if (currentPlayer == manager.startPlayer)
                if (c.getCardNumber() > 10)
                    bid += 0.75;
        }
        int roundedBid = (int)Math.round(bid);
        if (currentPlayer == (manager.startPlayer + 3) % 4 && (roundedBid + manager.addedGuesses == manager.potsFinished + 1)) {
            if (roundedBid == 0)
                roundedBid++;
            else
                roundedBid--;
        }
        return roundedBid;
    }

    public static int turnFiveToEightBid(int currentPlayer, BridgePlayer player, BridgeManager manager) {
        double bid = 0;
        List<Integer> trumpNums = new ArrayList<>();
        for (Card card : player.hand)
            if (card.getSuit().equals(manager.trumpSuit))
                trumpNums.add(card.getCardNumber());
        if (trumpNums.contains(14)) {
            trumpNums.remove(new Integer(14));
            bid++;
        }
        if (trumpNums.contains(13)) {
            trumpNums.remove(new Integer(13));
            bid++;
        }
        if (trumpNums.contains(12) && trumpNums.size() > 1) {
            trumpNums.remove(new Integer(12));
            bid++;
        }
        if (trumpNums.size() > 2) {
            bid += 0.49 * (trumpNums.size() - 2);
        }
        for (Card.Suit suit : Card.Suit.values()) {
            if (!(suit.equals(manager.trumpSuit))) {
                bid += suitBids(suit, player, bid < trumpNums.size());
            }
        }
        int roundedBid = (int)Math.round(bid);
        if (currentPlayer == (manager.startPlayer + 3) % 4) {
            if ((roundedBid + manager.addedGuesses) == manager.potsFinished + 1) {
                if (roundedBid == 0)
                    return 1;
                else
                    return (int)(roundedBid - 1);
            }
        }
        return roundedBid;
    }

    public static int turnNineToTwelveBid(BridgePlayer player, BridgeManager manager, int currentPlayer)  {
        //Count the number of applicable cards towards winning the pot - we know the count is less than the potsFinished/total hand size.
        List<Integer> trumpNums = new ArrayList<>();
        for (Card card : player.hand)
            if (card.getSuit().equals(manager.trumpSuit))
                trumpNums.add(card.getCardNumber());

        double trumpBid = 0;
        if (trumpNums.contains(14) && trumpNums.contains(12) && trumpNums.size() > 2)
            trumpBid += 2;
        else {
            if (trumpNums.contains(14))
                trumpBid++;
            if (trumpNums.contains(12) && trumpNums.size() > 2)
                trumpBid++;
        }
        if (trumpNums.contains(13) && trumpNums.contains(11) && trumpNums.size() > 3)
            trumpBid += 2;
        else if (trumpNums.contains(13) && trumpNums.size() > 1)
            trumpBid++;
        if (trumpNums.size() > 3)
            trumpBid = trumpBid + 1 + ((trumpNums.size() - 4) / 2);

        for (Card.Suit suit : Card.Suit.values()) {
            if (!(suit.equals(manager.trumpSuit))) {
                trumpBid += suitBids(suit, player, trumpBid < trumpNums.size());
            }
        }
        int roundedBid = (int)Math.round(trumpBid);
        if (currentPlayer == (manager.startPlayer + 3) % 4) {
            if ((roundedBid + manager.addedGuesses) == manager.potsFinished + 1) {
                if (trumpBid == 0)
                    return 1;
                else
                    return (roundedBid - 1);
            }
        }
        return roundedBid;
    }

    public static double suitBids(Card.Suit suit, BridgePlayer player, boolean extraTrump) {
        double bid = 0;
        List<Integer> suitNums = new ArrayList<>();
        for (Card card : player.hand)
            if (card.getSuit().equals(suit))
                suitNums.add(card.getCardNumber());

        if (suitNums.size() == 0 && extraTrump)
            return 1.5;
        else if (suitNums.size() == 1 && extraTrump)
            return 1;
        if (suitNums.contains(14))
            bid += 1;
        if (suitNums.contains(13) && suitNums.size() > 1)
            bid += 1;
        if (suitNums.contains(12) && suitNums.size() > 2)
            bid += 0.5;

        return bid;
    }

    // Returns the best possible move for the given player and state of game using the recursive maxN algorithm.
    public static Card chooseMove(int currentPlayer, BridgeManager manager, int level) {
        double[] maxVector = new double[] {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        //TODO - bestCard should be null. This temp fix simply negates any problem with cardSelectable/AI for the time being so nothing crashes!
        Card bestCard = manager.players[currentPlayer].hand.get(new Random().nextInt(manager.players[currentPlayer].hand.size()));
        for(int i = 0; i < manager.players[currentPlayer].hand.size(); i++) {
            if(manager.cardSelectable(manager.players[currentPlayer].hand.get(i),true,currentPlayer)) {
                Card chosenCard = manager.players[currentPlayer].hand.remove(i);

                Pair<Card, double[]> result = maxN((currentPlayer + 1) % 4, manager, level - 1, chosenCard);
                if (result.values[(currentPlayer)] > maxVector[(currentPlayer)]) {
                    maxVector = result.values;
                    bestCard = chosenCard;
                }

                //Put the card back for next iterations
                manager.players[currentPlayer].hand.add(i, chosenCard);
            }
        }
        return bestCard;
    }

    // The maxN recursive function with shallow pruning.
    // Returns a 4-tuple for index ij, where i is the ith index of the tuple and j is the jth node of the tree.
    private static Pair<Card,double[]> maxN(int currentPlayer, BridgeManager manager, int level, Card card) {
        // If we've hit a terminal node, return the evaluation vector, in this case each of the player scores.
        if(manager.players[0].hand.size() == 0 || manager.players[1].hand.size() == 0 || manager.players[2].hand.size() == 0 || manager.players[3].hand.size() == 0) {
            Pair<Card,double[]> result = new Pair<Card,double[]>();
            result.move = card;
            result.values = new double[] {manager.players[0].score, manager.players[1].score, manager.players[2].score,
                    manager.players[3].score};
            return result;
        }

        // If we don't wish to continue searching, return the evaluation vector and last move for the node.
        if(level == 0) {
            Pair<Card,double[]> result = new Pair<Card,double[]>();
            result.move = card;
            result.values = new double[]{evaluate(0, manager), evaluate(1, manager),
                    evaluate(2, manager), evaluate(3, manager)};
            return result;
        }

        // Otherwise, evaluate maxN for all child nodes and return the max possible vector and its corresponding move.
        double[] maxVector = new double[] {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for(int i = 0; i < manager.players[currentPlayer].hand.size(); i++) {
            if(manager.cardSelectable(manager.players[currentPlayer].hand.get(i),true,currentPlayer)) {
                Card chosenCard = manager.players[currentPlayer].hand.remove(i);
                Pair<Card, double[]> result = maxN((currentPlayer + 1) % 4, manager, level - 1, chosenCard);
                if (result.values[(currentPlayer)] > maxVector[(currentPlayer)])
                    maxVector = result.values;

                //Put the card back for next iterations
                manager.players[currentPlayer].hand.add(i, chosenCard);
            }
        }

        Pair<Card,double[]> result = new Pair<Card,double[]>();
        result.move = card;
        result.values = maxVector;
        return result;
    }

    // Given the manager, which represents the current state of the game, and the player of interest, return their heuristic value.
    private static double evaluate(int currentPlayer, BridgeManager manager) {
        BridgePlayer player = ((BridgePlayer)manager.players[currentPlayer]);
        double partialTricksRemaining = player.guess - player.obtained - 0.75 * findGuaranteedWinners(currentPlayer,manager), tricksRemaining;

        if(partialTricksRemaining > 0)
            tricksRemaining = partialTricksRemaining - 0.25 * findNeutrals(currentPlayer,manager);
        else
            tricksRemaining = partialTricksRemaining - 0.1 * findNeutrals(currentPlayer,manager);

        double pointBidEstimate = 10 * (player.hand.size() - tricksRemaining)/player.hand.size();
        double pointTricksEstimate = player.guess - tricksRemaining;

        //This is the final heuristic value determined.
        return pointBidEstimate + pointTricksEstimate + guessAllMissingBids(manager);
    }

    // Returns the number of opponents who we guess will miss their bids.
    // Each opponent is guessed to miss their bid if their tricks remaining value is less than -2 after considering all categories.
    private static int guessAllMissingBids(BridgeManager manager) {
        int missedBids = 0;
        for(int i = 0; i < manager.players.length; i++) {
            BridgePlayer player = ((BridgePlayer)manager.players[i]);
            double partialTricksRemaining = player.guess - player.obtained - 0.75 * findLikelyWinners(i, manager), tricksRemaining;

            if(partialTricksRemaining > 0)
                tricksRemaining = partialTricksRemaining - 0.25 * findNeutrals(i,manager);
            else
                tricksRemaining = partialTricksRemaining - 0.1 * findNeutrals(i,manager);

            if(tricksRemaining < -2)
                missedBids++;
        }
        return missedBids;
    }

    // Returns the number of guaranteed winners in the given player's hand.
    // Guaranteed winner cards are defined here as aces of the trump suit (therefore values 0-1).
    private static int findGuaranteedWinners(int currentPlayer, BridgeManager manager) {
        //If the aces, etc. have already been played, find the next guaranteed winners.
        int highestPossibleWinner = 14;
        while(!manager.isInPlayersHands(highestPossibleWinner, manager.trumpSuit) && manager.playerHasSuit(manager.trumpSuit))
            highestPossibleWinner--;

        int found = 0;
        for(Card card : ((BridgePlayer)manager.players[currentPlayer]).hand)
            if(card.getSuit() == manager.trumpSuit && card.getCardNumber() ==  highestPossibleWinner)
                found++;

        return found;
    }

    // Returns the number of guaranteed winners in the given player's hand.
    // Guaranteed winner cards are defined here as 2's of the trump suit (therefore values 0-1).
    private static int findGuaranteedLosers(int currentPlayer, BridgeManager manager) {
        int found = 0;
        for(Card card : ((BridgePlayer)manager.players[currentPlayer]).hand)
            if(card.getSuit() == manager.trumpSuit && card.getCardNumber() == 2)
                found++;

        return found;
    }

    // Returns the number of guaranteed winners in the given player's hand.
    // Likely loser cards are defined here as 10-15's of the trump suit.
    private static int findLikelyWinners(int currentPlayer, BridgeManager manager) {
        int found = 0;
        for(Card card : ((BridgePlayer)manager.players[currentPlayer]).hand)
            if(card.getSuit() == manager.trumpSuit && card.getCardNumber() > 10 && card.getCardNumber() < 15)
                found++;

        return found;
    }

    // Returns the number of guaranteed winners in the given player's hand.
    // Likely loser cards are defined here as 2-7's of the trump suit.
    private static int findLikelyLosers(int currentPlayer, BridgeManager manager) {
        int found = 0;
        for(Card card : ((BridgePlayer)manager.players[currentPlayer]).hand)
            if(card.getSuit() == manager.trumpSuit && card.getCardNumber() > 2 && card.getCardNumber() < 7)
                found++;

        return found;
    }

    // Returns the number of guaranteed winners in the given player's hand.
    // Neutral cards are defined here as 7-10's of the trump suit.
    private static int findNeutrals(int currentPlayer, BridgeManager manager) {
        int found = 0;
        for(Card card : ((BridgePlayer)manager.players[currentPlayer]).hand)
            if(card.getSuit() == manager.trumpSuit && card.getCardNumber() >= 7 && card.getCardNumber() <= 10)
                found++;

        return found;
    }
}

