package com.example.james.cardsuite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
    }

    //This should be removed, only for testing - processes the state of the game manager.
    public void confirmClick(View v) {
        if(!(manager.isGameOver())) {
            // choose and swap portion
            int swapRound = manager.getRoundCount() % 4;
            if (swapRound != 3 && currentPlayerInteracting != 4) {
                consoleOutput.setText("Player " + Integer.toString(currentPlayerInteracting) + " choose:");
                int chosen = Integer.parseInt(consoleInput.getText().toString());
                chosenLists.add(manager.chooseCards(currentPlayerInteracting,chosen));
                currentPlayerInteracting++;

                if(currentPlayerInteracting == 4) {
                    for (int i = 0; i < 4; i++) {
                        manager.swapCards(chosenLists.get(i), i, swapRound);
                    }
                } else
                    return;
            }

            // Done swapping by this point - this should only be called once, the turn directly when we're done swapping.
            // Find player with 2 of clubs - we do this here because the card may be swapped.
            if(!foundStartPlayer) {
                manager.findStartPlayer();
                foundStartPlayer = true;
            }

            //FIRST ROUGH DRAFT - ALL CODE AFTER THIS UNCONVERTED

            // Handles the pot stuff
            if(currentPotTurn != 13) {
                manager.potHandle();
                currentPotTurn++;
                return;
            }

            for (Player player : manager.players) {
                ((HeartsPlayer) player).scoreChange();
            }

            // reshuffles deck and increments round count for next round
            manager.reset();
            //RESET ALL VARIABLES FOR INCREMENTING THE GAME e.g. currentTurn, etc.
        }

        consoleOutput.setText("The winner is player " + manager.findWinner());
        Intent intent = new Intent(GameActivity.this,ResultsActivity.class);
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
