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
        Player[] players;
        Card pot, trumpCard;
        public RoundManager() {
            for(int i = 0; i < players; i++) 
                players[i] = new Player(roundNumber);
        }
        public int[] runRound(int[] originalScores) {
            String input = null;
            
            //Extract guesses...
            for(Player player : players) {
                input = //...
                player.setGuess(Integer.toString(input));
            }
            
            trumpCard = Card.cardFactory(); //Check for collisions of cards
            
            //Cycle through each player and retrieve their card inputs...
            for(Player player : players) {
                input = //...
                pot = //... retrieve the index from hand, check for validity
            }
            //do...
            //uses mutableDeck to draw cards from
        }
    }
    public GameManager(int players) {
        this.players = players;
        this.scores = new int[players];
        for(int i = 0; i < 52; i++)
            deck.add(Card.cardFactory());
    }
    
    public void runGame() {
        int maxCards = 0;
        while(maxCards < 52) {
            scores = new RoundManager().runRound(scores);
            maxCards = players * roundNumber;
        }
    }
    
    
}
