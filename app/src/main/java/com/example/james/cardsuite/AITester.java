package com.example.james.cardsuite;

import android.util.Log;

//Run this to test AI.
public class AITester {
    private static final int maxNLevelDepth = 3, trialsRun = 100;
    private static int[] wins = new int[4];

    //TODO - Implement general solution - below solution for bridge AI testing alone.
    //For now, players 1/2 are maxN AI while players 3/4 are random AI.
    public static void main(String[] args) {
        for(int i = 0; i < trialsRun; i++)
            wins[AITester.executeBridgeGame()]++;

        //Trials done, print statistics.
        System.out.println("Bridge AI win %: " + ((wins[0] + wins[1])*100 / trialsRun));
        System.out.println("Bridge AI wins: " + (wins[0]+wins[1]));
        System.out.println("Player 1 (Bridge AI) wins: " + wins[0]);
        System.out.println("Player 2 (Bridge AI) wins: " + wins[1]);


        System.out.println("Random AI win %: " + ((wins[2] + wins[3])*100 / trialsRun));
        System.out.println("Random AI wins: " + (wins[2] + wins[3]));
        System.out.println("Player 3 (Random AI) wins: " + wins[2]);
        System.out.println("Player 4 (Random AI) wins: " + wins[3]);
    }

    //Executes 1 game of German Bridge, returns the player number (0-3) that won.
    private static int executeBridgeGame() {
        BridgeManager manager = new BridgeManager();
        manager.totalRoundCount = 12;
        //Each loop iteration is a round, where the i'th round each player will start with i cards.
        int currentPlayer;
        for(int i = 0; i < manager.totalRoundCount - 1; i++) {
            currentPlayer = manager.startPlayer;

            //Bidding (Players 1,2 are maxN AI while 3,4 are random).
            ((BridgePlayer)(manager.players[0])).guess = BridgeAI.getBid(currentPlayer,manager);
            ((BridgePlayer)(manager.players[1])).guess = BridgeAI.getBid(currentPlayer, manager);
            ((BridgePlayer)(manager.players[2])).guess = RandomAI.getGermanBid(currentPlayer,manager);
            ((BridgePlayer)(manager.players[3])).guess = RandomAI.getGermanBid(currentPlayer,manager);

            //Continuing the execution of a round, until all hands' cards are gone.
            int turnsTaken = 0;
            int lastPlayer = (manager.startPlayer-1 % 4) + (manager.startPlayer-1 < 0 ? 4 : 0);
            while(!manager.getPlayers()[lastPlayer].hand.isEmpty()) {
                Card card = currentPlayer < 2 ? BridgeAI.chooseMove(currentPlayer,manager,maxNLevelDepth) : RandomAI.chooseMove(currentPlayer,manager);
                int chosen = manager.players[currentPlayer].hand.indexOf(card);

                germanPotHandle(chosen,currentPlayer,manager);
                currentPlayer = (currentPlayer + 1) % 4;
                turnsTaken++;

                if(turnsTaken == 4) {
                    manager.potAnalyze();
                    turnsTaken = 0;
                }
            }

            for(Player player : manager.players)
                player.scoreChange();
            manager.reset();
        }

        System.out.println(manager.players[0].score + "|" + manager.players[1].score + "|" + manager.players[2].score + "|" + manager.players[3].score);
        return manager.findWinner();
    }

    private static void germanPotHandle(int chosen, int currentPlayer, BridgeManager manager) {
        // other players choose cards - must place similar suit to startSuit if they have it.
        manager.pot.put(currentPlayer, manager.players[currentPlayer].hand.get(chosen));

        if(currentPlayer == manager.startPlayer) {
            // first player can choose any card he/she wants
            manager.startSuit = manager.players[manager.startPlayer].hand.remove(chosen).getSuit();
            return;
        }

        manager.players[currentPlayer].hand.remove(chosen);
    }
}
