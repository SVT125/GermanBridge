package com.example.james.cardsuite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class GameActivity extends Activity {
    private HeartsManager manager;
    private TextView consoleOutput;
    private EditText consoleInput;
    private int currentPlayerInteracting = 0, currentPotTurn = 0;
    private boolean foundStartPlayer = false;
    private List<List<Card>> chosenLists = new ArrayList<List<Card>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        manager = new HeartsManager();

        consoleOutput = (TextView)findViewById(R.id.consoleOutput);
        consoleInput = (EditText)findViewById(R.id.consoleInput);

        consoleOutput.setText("Player 1 choose:");
    }

    //This should be removed, only for testing - processes the state of the game manager.
    public void confirmClick(View v) {
        if(!(manager.isGameOver())) {
            // Part 1 - Swap the cards between players.
            int swapRound = manager.getRoundCount() % 4;
            if(currentPlayerInteracting != 4) {
                //Assume for now input will only be "card1,card2,card3"
                String[] chosen = consoleInput.getText().toString().split(",");
                int[] chosenIndices = new int[]{Integer.parseInt(chosen[0]),
                    Integer.parseInt(chosen[1]),
                    Integer.parseInt(chosen[2])};

                List<Card> chosenCards = manager.chooseCards(currentPlayerInteracting, chosenIndices);
                chosenLists.add(chosenCards);
            }

            if (swapRound != 3 && currentPlayerInteracting != 4) {
                currentPlayerInteracting++;
                consoleOutput.setText("Player " + Integer.toString(currentPlayerInteracting)+1 + " choose:");

                if(currentPlayerInteracting == 4) {
                    for (int i = 0; i < 4; i++) {
                        manager.swapCards(chosenLists.get(i), i, swapRound);
                        manager.players[i].organize();
                    }
                } else
                    return;
            }

            // Done swapping by this point - this should only be called once, the turn directly when we're done swapping.
            // Part 2 - Find player with 2 of clubs, we do this here because the card may be swapped.
            if(!foundStartPlayer) {
                manager.findStartPlayer();
                foundStartPlayer = true;
            }

            //FIRST ROUGH DRAFT - ALL CODE AFTER THIS UNCONVERTED

            // Part 3 - handle cards being tossed in the pot until all cards are gone (13 turns).
            if(currentPotTurn != 13) {
                manager.potHandle();
                currentPotTurn++;
                return;
            }

            // Part 4 - The round is done, update the score and reset the manager for the next round.
            for (Player player : manager.players) {
                ((HeartsPlayer) player).scoreChange();
            }

            // reshuffles deck and increments round count for next round
            manager.reset();
            //RESET ALL VARIABLES FOR INCREMENTING THE GAME e.g. currentTurn, etc.
        }

        // The game is done.
        consoleOutput.setText("The winner is player " + manager.findWinner());
        Intent intent = new Intent(GameActivity.this,ResultsActivity.class);
        intent.putExtra("manager",manager);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
