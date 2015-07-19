package com.example.james.cardsuite;

import android.app.Activity;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        manager = new HeartsManager();

        consoleOutput = (TextView)findViewById(R.id.consoleOutput);
        consoleInput = (EditText)findViewById(R.id.consoleInput);

        while(!(manager.isGameOver())) {
            // choose and swap portion
            int swapRound = manager.roundCount % 4;
            if (swapRound != 3) {
                List<List<Card>> chosenLists = new ArrayList<List<Card>>();

                for (int i = 0; i < 4; i++) {
                    consoleOutput.setText("Player " + Integer.toString(i) + " choose:");
                    int chosen = Integer.parseInt(consoleInput.getText().toString());
                    chosenLists.add(manager.chooseCards(i,chosen));
                }
                for (int i = 0; i < 4; i++) {
                    manager.swapCards(chosenLists.get(i), i, swapRound);
                }
            }

            // find player with 2 of clubs - we do this here because the card may be swapped.
            manager.findStartPlayer();

            // handles the pot stuff
            for (int i = 0; i < 13; i++) {
                manager.potHandle();
            }

            for (Player player : manager.players) {
                ((HeartsPlayer) player).scoreChange();
            }

            // reshuffles deck and increments round count for next round
            manager.reset();
        }
        consoleOutput.setText("The winner is player " + manager.findWinner());
    }

    //This should be removed, only for testing.
    public void confirmClick(View v) {

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
