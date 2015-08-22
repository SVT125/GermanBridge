package com.example.james.cardsuite;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SpadesActivity extends GameActivity {
    private int guess = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spades);

        Intent intent = getIntent();
        this.isSinglePlayer = intent.getBooleanExtra("isSinglePlayer", true);

        manager = new SpadesManager();

        //Display the image buttons
        displayHands(0);

        if(!isSinglePlayer)
            for(int i = 3; i >= 0; i--)
                openGuessDialog(i);
        else {
            for(int i = 0; i < 4; i++)
                if(i != manager.findStartPlayer())
                    ((SpadesPlayer)(manager.players[i])).bid = SpadesAI.getBid(i,(SpadesManager)manager);

            openGuessDialog(manager.findStartPlayer());
        }

        currentPlayerInteracting = manager.findStartPlayer();
        displayHands(manager.findStartPlayer());
        displayEndPiles(scores);
    }

    public void gameClick(View v) {
        super.gameClick(v);
        //Play sounds only if we're done swapping in hearts or are in any other game mode.
        if(finishedLoading) {
            int chosenSound = r.nextInt(3);
            soundPools[chosenSound].play(sounds[chosenSound], 1, 1, 0, 0, 1);
        }
        if(!(manager.isGameOver())) {
            int chosen = getCardIndex(v);

            manager.potHandle(chosen, currentPlayerInteracting);
            for (int i = 0; i < 4; i++)
                potClear();
            displayPot();

            //If this is singleplayer, have all the AI act to prepare the player's next click.
            if(isSinglePlayer) {
                for(int i = 1; i < 4; i++) {
                    int currentPlayer = (currentPlayerInteracting+i)%4;
                    displayHands(currentPlayer);
                    
                    Card bestMove = SpadesAI.chooseMove(currentPlayer,(SpadesManager)manager,levelsToSearch);
                    int chosenAI = manager.players[currentPlayer].hand.indexOf(bestMove);
                    manager.potHandle(chosenAI, currentPlayer);
                    for (int j = 0; j < 4; j++)
                        potClear();
                    displayPot();
                }
            } else
                currentPlayerInteracting = (currentPlayerInteracting + 1) % manager.playerCount;

            manager.players[currentPlayerInteracting].organize();
            displayHands(currentPlayerInteracting);

            if (manager.pot.size() == 4 && currentPotTurn != 13) {
                currentPotTurn++;
                manager.potAnalyze(); //sets the new start player for the next pot

                if(isSinglePlayer)
                    currentPlayerInteracting = 0;
                else
                    currentPlayerInteracting = manager.startPlayer;

                manager.players[currentPlayerInteracting].handsWon++;

                displayEndPiles(scores);
                manager.newRound();

                if (currentPotTurn != 13) {
                    displayHands(currentPlayerInteracting);
                    return;
                }
            } else if(manager.pot.size() != 4) {
                // If the pot reaches max size of 4, then we know to continue and compare cards
                return;
            }

            //If all the hands are exhausted, restart the entire game (until a score has reached 500).
            if(currentPotTurn == 13 && !manager.isGameOver()) {
                List<Integer> scores = new ArrayList<Integer>();
                for (Player player : manager.getPlayers()) {
                    player.scoreChange();
                    scores.add(player.score);
                }

                displayEndPiles(scores);
                reset();
                openGuessDialog(currentPlayerInteracting);
                return;
            }
        } else {
            // The game is done - pass all relevant information for results activity to display.
            // Passing manager just in case for future statistics if needbe.
            Intent intent = new Intent(SpadesActivity.this, ResultsActivity.class);
            intent.putExtra("manager", manager);
            intent.putExtra("players", manager.getPlayers());
            startActivity(intent);
            finish();
        }
    }

    // reshuffles deck, increments round count, resets all variables for the next round.
    public void reset() {
        manager.reset();
        finishedSwapping = false; buttonsPresent = false;
        currentPotTurn = 0; currentPlayerInteracting = 0;
        displayHands(0);
    }

    //Call when the end piles and the scores displayed on top of the piles need be redisplayed.
    public void displayEndPiles(List<Integer> scores) {
        TextView[] scoreViews = new TextView[] {(TextView)findViewById(R.id.bottomScore), (TextView)findViewById(R.id.leftScore),
                (TextView)findViewById(R.id.topScore), (TextView)findViewById(R.id.rightScore)};
        ImageView[] pileViews = new ImageView[] {(ImageView)findViewById(R.id.bottomPile), (ImageView)findViewById(R.id.leftPile),
                (ImageView)findViewById(R.id.topPile), (ImageView)findViewById(R.id.rightPile)};

        for(int i = 0; i < 4; i++) {
            //Update the score, but remove or update the pile if it exists.
            if ((manager.getPlayers()[i].handsWon) > 0) {
                scoreViews[i].setText(Integer.toString(manager.getPlayers()[i].handsWon));
                scoreViews[i].setVisibility(View.VISIBLE);
            } else {
                scoreViews[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    //Opens the guess dialog - fit for German Bridge for now.
    public void openGuessDialog(final int currentPlayer) {
        displayHands(currentPlayer);

        AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Holo_Panel);
        builder.setCancelable(false);
        builder.setTitle("Player " + (currentPlayer + 1) + " bids");
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i <= manager.players[currentPlayer].hand.size(); i++) {
            Button guessButton = new Button(this);
            guessButton.setText(Integer.toString(i));
            guessButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    guess = Integer.parseInt(((TextView) v).getText().toString());
                }
            });
            layout.addView(guessButton);
        }

        horizontalScrollView.addView(layout);
        builder.setView(horizontalScrollView);
        builder.setPositiveButton("OK", null);

        final AlertDialog d = builder.create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button okButton = d.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (guess != -1) {
                            ((SpadesPlayer) manager.players[currentPlayer]).bid = guess;
                            switch(currentPlayer) {
                                case 0: ((SpadesPlayer) manager.players[0]).totalBid = ((SpadesPlayer) manager.players[2]).totalBid =
                                        ((SpadesPlayer) manager.players[0]).bid + ((SpadesPlayer) manager.players[2]).bid; break;
                                case 1: ((SpadesPlayer) manager.players[1]).totalBid = ((SpadesPlayer) manager.players[3]).totalBid =
                                        ((SpadesPlayer) manager.players[1]).bid + ((SpadesPlayer) manager.players[3]).bid; break;
                                case 2: ((SpadesPlayer) manager.players[0]).totalBid = ((SpadesPlayer) manager.players[2]).totalBid =
                                        ((SpadesPlayer) manager.players[0]).bid + ((SpadesPlayer) manager.players[2]).bid; break;
                                case 3: ((SpadesPlayer) manager.players[1]).totalBid = ((SpadesPlayer) manager.players[3]).totalBid =
                                        ((SpadesPlayer) manager.players[1]).bid + ((SpadesPlayer) manager.players[3]).bid; break;
                            }
                            d.dismiss();
                        }
                    }
                });
            }
        });
        d.show();
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
        RelativeLayout left = (RelativeLayout)findViewById(R.id.leftPlayerHandLayout),
                top = (RelativeLayout)findViewById(R.id.topPlayerHandLayout),
                right = (RelativeLayout)findViewById(R.id.rightPlayerHandLayout),
                bottom = (RelativeLayout)findViewById(R.id.bottomPlayerHandLayout);

        //Now create the imagebuttons for each of the players.
        //Note: other possible param values for initialTheta scalar and deltaY scalar are (5,3).
        for(int i = 0; i < 4; i++) {
            //The coordinate and angular offsets for every card. Theta is dependent on the number of cards in the hand.
            int deltaX = 0, deltaY;
            float initialTheta= (float)-4.6*manager.getPlayers()[i].hand.size()/2;
            for(int j = 0; j < manager.getPlayers()[i].hand.size(); j++) {
                float theta = (float)(initialTheta + 4.6*j);
                deltaY = (int)(2.5*(30 - Math.pow(j - manager.getPlayers()[i].hand.size()/2,2))); //Truncate the result of the offset
                RelativeLayout.LayoutParams restParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);

                //How to treat and initialize the other cards depending on whether the current player or any other.
                ImageView cardButton;
                if(i == player) {
                    cardButton = new ImageButton(this);
                    cardButton.setImageResource(getResources().getIdentifier(manager.getPlayers()[player].hand.get(j).getAddress(), "drawable", getPackageName()));
                    cardButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            gameClick(v);
                        }
                    });

                    //Tint and make the card unselectable if it's not meant to be.
                    Card selectCard = manager.getPlayers()[i].hand.get(j);
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
                    case 0: restParams.setMargins(deltaX,95-deltaY,0,0);
                        cardButton.setRotation(theta);
                        bottom.addView(cardButton, restParams); break;
                    case 1: restParams.setMargins(100+deltaY,deltaX,0,0);
                        cardButton.setRotation(90 + theta);
                        left.addView(cardButton, restParams); break;
                    case 2: restParams.setMargins(deltaX,60+deltaY,0,0);
                        cardButton.setRotation(180 - theta);
                        top.addView(cardButton, restParams); break;
                    case 3: restParams.setMargins(115-deltaY,deltaX,0,0);
                        cardButton.setRotation(90 - theta);
                        right.addView(cardButton, restParams); break;
                }
                cardButton.setId(temporaryID++);

                //Set the deltaX/theta parameters for the next card/loop iteration.
                //Consequence of more space horizontally than vertically; set smaller distance between cards vertically.
                deltaX = (i % 2 == 0) ? deltaX + 60 : deltaX + 55;
            }
        }
        buttonsPresent = true;
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spades, menu);
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
