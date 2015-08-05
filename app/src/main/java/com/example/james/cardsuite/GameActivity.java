package com.example.james.cardsuite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class GameActivity extends Activity {
    private HeartsManager manager;
    private TextView consoleOutput;
    private int currentPlayerInteracting = 0, currentPotTurn = 0;
    private boolean foundStartPlayer = false, buttonsPresent = false, finishedSwapping = false, beganPot = false;
    public boolean initialOutputWritten = false;
    private List<List<Card>> chosenLists = new ArrayList<List<Card>>();
    private List<Integer> chosenIndices = new ArrayList<Integer>();
    private List<List<Card>> originalHands;
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
            int chosenID = v.getId() % (13 - manager.getRoundCount() + 1), chosen = -1;
            Card chosenCard = originalHands.get(currentPlayerInteracting).get(chosenID);
            for(int i = 0; i < manager.players[currentPlayerInteracting].hand.size(); i++) {
                if(manager.players[currentPlayerInteracting].hand.get(i).compareTo(chosenCard) == 0) {
                    chosen = i;
                    break;
                }
            }

            //Select or unselect the card with a border shown
            if(chosenCard.isClicked == false) {
                v.setBackgroundResource(R.drawable.card_border);
                chosenCard.isClicked = true;
            } else {
                v.setBackgroundResource(0);
                chosenCard.isClicked = false;
            }

            if(!finishedSwapping) {
                // Part 1 - Swap the cards between players.
                int swapRound = manager.getRoundCount() % 4;
                if (currentPlayerInteracting != 4) {
                    //If the chosen card is already chosen, unselect it - otherwise, add it to our chosen cards.
                    if (chosenIndices.contains((Integer)chosen))
                        chosenIndices.remove((Integer)chosen);
                    else {
                        chosenIndices.add((Integer)chosen);
                    }

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
                    } else {
                        manager.players[currentPlayerInteracting].organize();
                        displayHands(currentPlayerInteracting);
                        return;
                    }
                }
            }

            // Done swapping by this point - this should only be called once, the turn directly when we're done swapping.
            // Part 2 - Find player with 2 of clubs, we do this here because the card may be swapped.
            if(!foundStartPlayer) {
                manager.findStartPlayer();
                foundStartPlayer = true;
                currentPlayerInteracting = manager.startPlayer;
            }

            // Part 3 - handle cards being tossed in the pot until all cards are gone (13 turns).
            if(manager.potHandle(consoleOutput, chosen, currentPlayerInteracting, initialOutputWritten, this)) {
                currentPlayerInteracting = (currentPlayerInteracting + 1) % 4;

                //If we're on the start player in the beginning, we want to return at this point; if we're done with the pot, continue.
                if (currentPlayerInteracting == manager.startPlayer && !beganPot) {
                    beganPot = true;
                    return;
                }
            }

            //End of a single pot round, reset all variables for the next pot round if possible.
            if(currentPlayerInteracting == manager.startPlayer && currentPotTurn != 13 && beganPot) {
                currentPotTurn++;

                manager.potAnalyze(); //sets the new start player for the next pot
                for (Card c : manager.pot.keySet()) {
                    ((HeartsPlayer)manager.players[manager.startPlayer]).endPile.add(c);
                }

                manager.pot.clear();
                displayHands(manager.startPlayer);

                beganPot = false;
                finishedSwapping = false;
                initialOutputWritten = false;

                return;
            }

            // Part 4 - The round is done, update the score and reset the manager for the next round.
            if(currentPotTurn == 13) {
                for (Player player : manager.players) {
                    ((HeartsPlayer) player).scoreChange();
                }

                // reshuffles deck, increments round count, resets all variables for the next round.
                manager.reset();
            }
        } else {
            // The game is done.
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
                if(view != null)
                    ((ViewGroup) view.getParent()).removeView(view);
            }
        }

        if(originalHands == null) {
            originalHands = new ArrayList<>();
            for (Player p : manager.players)
                originalHands.add(p.hand);
        }

        int temporaryID = 0; //Temporary ID to be assigned to each card, to be reused.
        RelativeLayout left = (RelativeLayout)findViewById(R.id.leftPlayerLayout),
                top = (RelativeLayout)findViewById(R.id.topPlayerLayout),
                right = (RelativeLayout)findViewById(R.id.rightPlayerLayout),
                bottom = (RelativeLayout)findViewById(R.id.bottomPlayerLayout);

        //Now create the imagebuttons for each of the players
        for(int i = 0; i < 4; i++) {
            //Display the rest of the hand
            int offsetMargin = 0; //The offset between cards, this should be relative to the first card and is summed over additional cards
            for(int j = 0; j < manager.players[player].hand.size(); j++) {
                RelativeLayout.LayoutParams restParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

                //Set the offset between cards, left of/top of depending on player.
                if(i % 2 == 0)
                    restParams.setMargins(offsetMargin,0,0,0);
                else
                    restParams.setMargins(0,offsetMargin,0,0);

                //How to treat and initialize the other cards depending on whether the current player or any other.
                ImageView cardButton;
                if(i == player) {
                    cardButton = new ImageButton(this);
                    cardButton.setImageResource(getResources().getIdentifier(manager.players[player].hand.get(j).getAddress(), "drawable", getPackageName()));
                    cardButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmClick(v);
                        }
                    });
                } else {
                    cardButton = new ImageView(this);
                    cardButton.setImageResource(getResources().getIdentifier("cardback", "drawable", getPackageName()));
                }

                cardButton.setPadding(2,2,2,2);
                cardButton.setLayoutParams(restParams);

                switch(i) {
                    case 0: bottom.addView(cardButton); break;
                    case 1: cardButton.setRotation(90);
                        left.addView(cardButton); break;
                    case 2: top.addView(cardButton); break;
                    case 3: cardButton.setRotation(270);
                        right.addView(cardButton); break;
                }
                cardButton.setId(temporaryID++);
                offsetMargin += 65;
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
