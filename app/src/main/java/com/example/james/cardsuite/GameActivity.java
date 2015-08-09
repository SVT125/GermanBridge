package com.example.james.cardsuite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
    private boolean foundStartPlayer = false, buttonsPresent = false, finishedSwapping = false;
    public boolean initialOutputWritten = false;
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
        displayScores();
    }

    //Processes the state of the game manager.
    public void confirmClick(View v) {
        if(!(manager.isGameOver())) {
            int chosen = v.getId();
            for(int i = 0; i < currentPlayerInteracting; i++)
                chosen -= manager.players[i].hand.size();

            Card chosenCard = manager.players[currentPlayerInteracting].hand.get(chosen);


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
                int swapRound = manager.getPotsFinished() % 4;
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
                if(currentPlayerInteracting == manager.startPlayer)
                    for(int i = 0; i < 4; i++)
                        potClear();

                displayPot();
                currentPlayerInteracting = (currentPlayerInteracting + 1) % 4;

                // If the pot reaches max size of 4, then we know to continue and compare cards
                if (manager.pot.size() != 4) {
                    return;
                }
            }
            else {
                return;
            }

            //End of a single pot round, reset all variables for the next pot round if possible.
            if(currentPlayerInteracting == manager.startPlayer && currentPotTurn != 13) {
                currentPotTurn++;

                manager.potAnalyze(); //sets the new start player for the next pot
                currentPlayerInteracting = manager.startPlayer;
                for (Card c : manager.pot.values()) {
                    ((HeartsPlayer)manager.players[manager.startPlayer]).endPile.add(c);
                }

                displayScores();
                manager.pot.clear();
                manager.newRound();

                if (currentPotTurn != 13) {
                    consoleOutput.setText("Player " + Integer.toString(currentPlayerInteracting + 1) + " wins the pot! Place a card to begin next round");
                    displayHands(manager.startPlayer);
                    return;
                }
            }

            // Part 4 - The round is done, update the score and reset the manager for the next round.
            if(currentPotTurn == 13 && !manager.isGameOver()) {
                List<Integer> scores = new ArrayList<Integer>();
                boolean shootMoon = false;
                for (Player player : manager.players) {
                    shootMoon = ((HeartsPlayer) player).scoreChange();
                    if (shootMoon) {
                        for (Player otherPlayers : manager.players) {
                            if (!otherPlayers.equals(player)) {
                                otherPlayers.score += 26;
                            }
                        }
                    }
                    scores.add(player.score);
                }

                displayScores();
                reset();
                return;
            }
        } else {
            // The game is done - pass all relevant information for results activity to display.
            // Passing manager just in case for future statistics if needbe.
            Intent intent = new Intent(GameActivity.this, ResultsActivity.class);
            intent.putExtra("manager", manager);
            intent.putExtra("players", manager.players);
            startActivity(intent);
        }
        finish();
    }

    // reshuffles deck, increments round count, resets all variables for the next round.
    public void reset() {
        manager.reset();
        finishedSwapping = false; initialOutputWritten = false; buttonsPresent = false; foundStartPlayer = false;
        currentPotTurn = 0; currentPlayerInteracting = 0;
        displayHands(0);
        chosenLists.clear();
        chosenIndices.clear();
    }

    // Called when a player places a valid card into the pot; updates the images in the pot
    public void displayPot() {
        RelativeLayout potLayout = (RelativeLayout)findViewById(R.id.potLayout);
        for(int i = 0; i < manager.pot.size(); i++) {
            int index = (manager.startPlayer + i) % 4;
            ImageView potCard = new ImageView(this);
            potCard.setImageResource(getResources().getIdentifier(manager.pot.get(index).getAddress(), "drawable", getPackageName()));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            switch(index) {
                case 0: params.setMargins(0, 25, 0, 0);
                    params.addRule(RelativeLayout.BELOW, R.id.anchor); break;
                case 1: params.setMargins(0,0,45,0);
                    params.addRule(RelativeLayout.LEFT_OF, R.id.anchor);
                    potCard.setRotation(90); break;
                case 2: params.setMargins(0,0,0,25);
                    params.addRule(RelativeLayout.ABOVE, R.id.anchor);
                    potCard.setRotation(180); break;
                case 3: params.setMargins(45,0,0,0);
                    params.addRule(RelativeLayout.RIGHT_OF, R.id.anchor);
                    potCard.setRotation(270); break;
            }

            potCard.setElevation(i);
            potCard.setId(100 + index); //Set the convention that all the pot card views take values 100-103 for all 4 cards.
            potCard.setLayoutParams(params);
            potLayout.addView(potCard);
        }
    }

    // Called at the end of a round when all four players have added their cards; clears the pot using given IDs 100-103.
    public void potClear() {
        for(int i = 0; i < 4; i++) {
            View view = findViewById(100 + i);
            if(view != null)
                ((ViewGroup) view.getParent()).removeView(view);
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

        int temporaryID = 0; //Temporary ID to be assigned to each card, to be reused.
        RelativeLayout left = (RelativeLayout)findViewById(R.id.leftPlayerCardsLayout),
                top = (RelativeLayout)findViewById(R.id.topPlayerCardsLayout),
                right = (RelativeLayout)findViewById(R.id.rightPlayerCardsLayout),
                bottom = (RelativeLayout)findViewById(R.id.bottomPlayerCardsLayout);

        //Now create the imagebuttons for each of the players
        for(int i = 0; i < 4; i++) {
            //Display the rest of the hand
            int offsetMargin = 0; //The offset between cards, this should be relative to the first card and is summed over additional cards
            for(int j = 0; j < manager.players[i].hand.size(); j++) {
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

                    //Tint and make the card unselectable if it's not meant to be.
                    Card selectCard = manager.players[i].hand.get(j);
                    if(!(manager.cardSelectable(selectCard, finishedSwapping, i))) {
                            cardButton.setColorFilter(Color.parseColor("#78505050"), PorterDuff.Mode.SRC_ATOP);
                            cardButton.setClickable(false);
                        }
                    }
                else {
                    cardButton = new ImageView(this);
                    cardButton.setImageResource(getResources().getIdentifier("cardback", "drawable", getPackageName()));
                }

                cardButton.setPadding(3, 3, 3, 3);

                switch(i) {
                    case 0: restParams.bottomMargin = 75;
                        bottom.addView(cardButton, restParams); break;
                    case 1: restParams.leftMargin = 100;
                        cardButton.setRotation(90);
                        left.addView(cardButton, restParams); break;
                    case 2: restParams.topMargin = 75;
                        top.addView(cardButton, restParams); break;
                    case 3: restParams.leftMargin = 25;
                        cardButton.setRotation(270);
                        right.addView(cardButton, restParams); break;
                }
                cardButton.setId(temporaryID++);
                offsetMargin += 65;
            }
        }
        buttonsPresent = true;
    }

    public void displayScores() {
        TextView bottomOutput = (TextView)findViewById(R.id.bottomPlayerOutput),
                leftOutput = (TextView)findViewById(R.id.leftPlayerOutput),
                topOutput = (TextView)findViewById(R.id.topPlayerOutput),
                rightOutput = (TextView)findViewById(R.id.rightPlayerOutput);

        bottomOutput.setText("Player 1 | Score: " + Integer.toString(manager.players[0].score));
        leftOutput.setText("Player 2 | Score: " + Integer.toString(manager.players[1].score));
        topOutput.setText("Player 3 | Score: " + Integer.toString(manager.players[2].score));
        rightOutput.setText("Player 4 | Score: " + Integer.toString(manager.players[3].score));
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
