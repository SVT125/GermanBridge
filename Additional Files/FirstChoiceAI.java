package com.example.james.cardsuite;

//AI that takes the first choice/card/etc.
public class FirstChoiceAI {
    public static Card chooseMove(int currentPlayer, Manager manager) {
        int index = 0;
        Card card;
        do {
            card = manager.players[currentPlayer].hand.get(index);
            index++;
        } while(!manager.cardSelectable(card,true,currentPlayer));

        return card;
    }

    //Bidding for GermanBridge.
    public static int getGermanBid(int currentPlayer, BridgeManager manager) {
        int guessedBid = 0;
        while(guessedBid != manager.getPlayers()[currentPlayer].hand.size() - manager.addedGuesses) {
            guessedBid++;
        }
        return guessedBid;
    }
}
