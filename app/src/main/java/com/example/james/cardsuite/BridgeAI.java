package com.example.james.cardsuite;

import java.util.Random;

// The German Bridge AI. Heuristic eval function sourced mostly from http://www.fongboy.com/programs/Whist/whist_paper.pdf with Luckhardt/Irani's MaxN algorithm.
// Will also consider http://www.cs.umd.edu/~bswilson/presentation.pdf for socially oriented evaluations and varying difficulties.
public class BridgeAI {
    // Returns the suggested number of bids for the given player.
    // TODO: Find a more comprehensive bidding function...
    public int getBid(int currentPlayer, BridgeManager manager) {
        int guessedBid = manager.getPlayers()[currentPlayer].hand.size() - manager.addedGuesses;
        Random r = new Random();
        while(guessedBid != manager.getPlayers()[currentPlayer].hand.size() - manager.addedGuesses) {
            guessedBid = r.nextInt(manager.potsFinished);
        }
        return guessedBid;
    }

    // The maxN recursive function with shallow pruning.
    // Returns a 4-tuple for index ij, where i is the ith index of the tuple and j is the jth node of the tree.
    public static Pair<Card,double[]> maxN(int currentPlayer, BridgeManager manager, int level, int turnsLeft, Card card) {
        // If we've hit a terminal node or don't wish to continue searching, return the evaluation vector and last move for the node.
        if(turnsLeft == 0 || level == 0) {
            Pair<Card,double[]> result = new Pair<Card,double[]>();
            result.move = card;
            result.values = new double[]{evaluate(0, manager), evaluate(1, manager),
                        evaluate(2, manager), evaluate(3, manager)};
            return result;
        }else {
            // Otherwise, Evaluate maxN for all child nodes and return the max possible vector and its corresponding move.
            double[] maxVector = new double[] {Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE};
            Card maxCard = null;
            for(int i = 0; i < manager.players[i].hand.size(); i++) {
                Card chosenCard = manager.players[i].hand.remove(i);
                Pair<Card, double[]> result = maxN((currentPlayer+1)%4,manager,level-1,turnsLeft-1, chosenCard);
                if(result.values[(currentPlayer-1)%4] > maxVector[(currentPlayer-1)%4]) {
                    maxCard = result.move;
                    maxVector = result.values;
                }

                //Put the card back for next iterations
                manager.players[i].hand.add(chosenCard);
            }

            Pair<Card,double[]> result = new Pair<Card,double[]>();
            result.move = maxCard;
            result.values = maxVector;
            return result;
        }
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
            double partialTricksRemaining = player.guess - player.obtained - 0.75 * findLikelyWinners(i,manager), tricksRemaining;

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
        int found = 0;
        for(Card card : ((BridgePlayer)manager.players[currentPlayer]).hand)
            if(card.getSuit() == manager.trumpSuit && card.getCardNumber() == 15)
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