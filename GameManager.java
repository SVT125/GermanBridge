/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package germanbridge;

import java.util.*;
/**
 *
 * @author James
 */
public class GameManager {
    private int[] scores;
    private int roundNumber = 0, players;
    private final Set<Card> deck;
    
    class RoundManager {
        Set<Card> mutableDeck = deck;
        int handSize;
        Player[] players;
        Card pot, trumpCard;
        public RoundManager() {
            for(int i = 0; i < players; i++) 
                players[i] = new Player(roundNumber);
        }
        public void runRound() {
            //do...
        }
    }
    public GameManager(int players) {
        this.players = players;
        this.scores = new int[players];
        //Initialize the deck...
    }
    
    public void runGame() {
        int maxCards = 0;
        while(maxCards < 52) {
            //run rounds
            
            maxCards = players * roundNumber;
        }
    }
    
    
}
