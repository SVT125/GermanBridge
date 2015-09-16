package com.example.james.cardsuite;

import java.util.Random;

// AI for testing purposes only - selects a random card out of the given player's hand, regardless of game.
// TODO - Make more comprehensive towards all games (e.g. hearts swapping).
public class RandomAI {
    public static Card chooseMove(int currentPlayer, Manager manager) {
        Random r = new Random();
        Card card;
        do {
            int chosenIndex = r.nextInt(manager.players[currentPlayer].hand.size());
            card = manager.players[currentPlayer].hand.get(chosenIndex);
        } while(!manager.cardSelectable(card,true,currentPlayer));

        return card;
    }

    //Bidding for GermanBridge.
    public static int getGermanBid(int currentPlayer, BridgeManager manager) {
        int guessedBid = manager.getPlayers()[currentPlayer].hand.size() - manager.addedGuesses;
        Random r = new Random();
        while(guessedBid != manager.getPlayers()[currentPlayer].hand.size() - manager.addedGuesses) {
            guessedBid = r.nextInt(manager.potsFinished);
        }
        return guessedBid;
    }
}
