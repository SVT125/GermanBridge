package com.example.james.cardsuite;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class GameActivity extends Activity {
    private HeartsManager manager;
    private TextView consoleOutput;
    private int currentPlayerInteracting = 0, currentPotTurn = 0;
    private boolean foundStartPlayer = false, buttonsPresent = false, finishedSwapping = false;
    public boolean outputWritten = false;
    private List<List<Card>> chosenLists = new ArrayList<List<Card>>();
    private List<Integer> chosenIndices = new ArrayList<Integer>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        manager = new HeartsManager();

        consoleOutput = (TextView)findViewById(R.id.consoleOutput);

        //Set the instruction text
        consoleOutput.setText("Player 1 chooses a card to swap");

        //Display the image buttons
        displayHands(0);
    }

    //Processes the state of the game manager.
    public void confirmClick(View v) {
        if(!(manager.isGameOver())) {
            int chosen = v.getId() % (13 - manager.getRoundCount());
            if(!finishedSwapping) {
                // Part 1 - Swap the cards between players.
                int swapRound = manager.getRoundCount() % 4;
                if (currentPlayerInteracting != 4) {
                    chosenIndices.add(chosen);

                    if (chosenIndices.size() < 3)
                        return;

                    List<Card> chosenCards = manager.chooseCards(currentPlayerInteracting, chosenIndices);
                    chosenLists.add(chosenCards);
                }

                if (swapRound != 3 && currentPlayerInteracting != 4) {
                    currentPlayerInteracting++;
                    consoleOutput.setText("Player " + Integer.toString(currentPlayerInteracting + 1) + " chooses a card to swap");
                    chosenIndices = new ArrayList<Integer>(); //restart the indices chosen for the next player

                    if (currentPlayerInteracting == 4) {
                        for (int i = 0; i < 4; i++) {
                            manager.swapCards(chosenLists.get(i), i, swapRound);
                            manager.players[i].organize();
                            displayHands(0);
                            currentPlayerInteracting = 0;
                        }
                        finishedSwapping = true;
                    } else
                        return;
                }
            }



            // Done swapping by this point - this should only be called once, the turn directly when we're done swapping.
            // Part 2 - Find player with 2 of clubs, we do this here because the card may be swapped.
            if(!foundStartPlayer) {
                manager.findStartPlayer();
                foundStartPlayer = true;
            }

            // Part 3 - handle cards being tossed in the pot until all cards are gone (13 turns).
            manager.potHandle(this, consoleOutput, chosen, currentPlayerInteracting);
            if(currentPlayerInteracting == 3 && currentPotTurn != 13) {
                currentPlayerInteracting = 0;
                currentPotTurn++;

                manager.potAnalyze();
                for (Card c : manager.pot.keySet()) {
                    ((HeartsPlayer)manager.players[manager.startPlayer]).addToPile(c);
                }
                manager.pot.clear();
                displayHands(manager.startPlayer);
                return;
            }

            // Part 4 - The round is done, update the score and reset the manager for the next round.
            if(currentPotTurn == 13) {
                for (Player player : manager.players) {
                    ((HeartsPlayer) player).scoreChange();
                }

                // reshuffles deck and increments round count for next round
                manager.reset();
                //RESET ALL VARIABLES FOR INCREMENTING THE GAME e.g. currentTurn, etc.
            }
        }

        // The game is done.
        if(manager.isGameOver()) {
            Intent intent = new Intent(GameActivity.this, ResultsActivity.class);
            intent.putExtra("manager", manager);
            startActivity(intent);
            finish();
        }
    }

    //Call when the hands have been updated and need be redisplayed.
    public void displayHands(int player) {
        //Remove all old cards first
        if(buttonsPresent) {
            for (int i = 0; i < 52; i++) {
                View view = findViewById(i);
                ((ViewGroup) view.getParent()).removeView(view);
            }
        }

        int temporaryID = 0; //Temporary ID to be assigned to each card, to be reused.

        //Now create the imagebuttons for each of the players
        for(int i = 0; i < 4; i++) {
            LinearLayout left = (LinearLayout)findViewById(R.id.leftPlayerLayout),
                    top = (LinearLayout)findViewById(R.id.topPlayerLayout),
                    right = (LinearLayout)findViewById(R.id.rightPlayerLayout),
                    bottom = (LinearLayout)findViewById(R.id.bottomPlayerLayout);

            ImageView firstCard, last;

            //Display the rest of the hand
            for(int j = 0; j < manager.players[player].hand.size(); j++) {
                RelativeLayout.LayoutParams restParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

                //How to treat and initialize the other cards depending on whether the current player or any other.
                ImageView cardButton;
                if(i == player) {
                    cardButton = new ImageButton(this);
                    cardButton.setBackgroundResource(getResources().getIdentifier(manager.players[player].hand.get(j).getAddress(), "drawable", getPackageName()));
                    cardButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmClick(v);
                        }
                    });
                } else {
                    cardButton = new ImageView(this);
                    cardButton.setBackgroundResource(getResources().getIdentifier("cardback", "drawable", getPackageName()));
                }

                switch(i) {
                    case 0: bottom.addView(cardButton); break;
                    case 1: left.addView(cardButton); break;
                    case 2: top.addView(cardButton); break;
                    case 3: right.addView(cardButton); break;
                }
                cardButton.setId(temporaryID++);
            }
        }
        buttonsPresent = true;
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
