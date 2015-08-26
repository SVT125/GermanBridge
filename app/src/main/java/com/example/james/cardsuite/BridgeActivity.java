package com.example.james.cardsuite;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.HashMap;
import java.util.List;

public class BridgeActivity extends GameActivity {
    private int guess = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bridge);

        Intent intent = getIntent();
        this.isBot = intent.getBooleanArrayExtra("isBot");

        //currentPlayerInteracting default-init'd to 0, we increment until we find a non-bot player.
        while(isBot[currentPlayerInteracting]) {
            currentPlayerInteracting++;
        }
        
        manager = new BridgeManager(currentPlayerInteracting);
        manager.totalRoundCount = 12; // Change later for variable number of players

        ImageView trumpView = (ImageView)findViewById(R.id.trumpView);
        trumpView.setVisibility(View.VISIBLE);
        Card trumpCard = ((BridgeManager)manager).trumpCard;
        trumpView.setImageResource(getResources().getIdentifier(trumpCard.getAddress(), "drawable", getPackageName()));

        for(int i = 3; i >= 0; i--) {
            if(isBot[i])
                ((BridgePlayer)(manager.players[i])).guess = BridgeAI.getBid(i,(BridgeManager)manager);
            else
                openGuessDialog(i);
        }
        //Display the image buttons
        displayEndPiles(scores);
        displayHands(currentPlayerInteracting);
    }

    public void gameClick(View v) {
        super.gameClick(v);
        //Play sounds only if we're done swapping in hearts or are in any other game mode.
        if(finishedLoading) {
            int chosenSound = r.nextInt(3);
            soundPools[chosenSound].play(sounds[chosenSound],1,1,0,0,1);
        }
        //Get the index of the chosen card in the current player's hand.
        int chosen = getCardIndex(v);

        // players start putting cards into the pot and calculate score
        if(manager.potsFinished <= manager.totalRoundCount) {
            manager.potHandle(chosen, currentPlayerInteracting);
            for (int i = 0; i < 4; i++)
                potClear();
            displayPot();

            executeAITurns();

            //If the pot is full (all players have tossed a card), reset the pot, analyze it, find the new start player/winner of the pot.
            if(manager.pot.size() == 4) {
                manager.potAnalyze();
                manager.pot = new HashMap<Integer,Card>();
                currentPlayerInteracting = manager.startPlayer;
                executeAITurns();
                for (int i = 0; i < 4; i++)
                    potClear();
                displayPot();
                displayEndPiles(scores);
            }

            displayHands(currentPlayerInteracting);

            //Set up the next round, reset all variables.
            int lastPlayer = manager.startPlayer == 0 ? 3 : manager.startPlayer - 1;
            if(manager.getPlayers()[lastPlayer].hand.isEmpty()) {
                scores.clear();
                for (Player player : manager.players) {
                    player.scoreChange();
                    scores.add(player.score);
                }

                // resets deck, hands, etc. and increments round
                reset();

                displayEndPiles(scores);

                //Cycle through any AI players for the first non-AI player.
                executeAITurns();

                for(int i = 3; i >= 0; i--) {
                    if(isBot[i])
                        ((BridgePlayer)(manager.players[i])).guess = BridgeAI.getBid(i,(BridgeManager)manager);
                    else
                        openGuessDialog(i);
                }

                displayHands(currentPlayerInteracting);

                //Redisplay the trump
                ImageView trumpView = (ImageView)findViewById(R.id.trumpView);
                Card trumpCard = ((BridgeManager)manager).trumpCard;
                trumpView.setImageResource(getResources().getIdentifier(trumpCard.getAddress(), "drawable", getPackageName()));
            }

            //If this wasn't the last round, return; otherwise, the game is finished.
            if(manager.potsFinished < manager.totalRoundCount - 1)
                return;
        }

        // The game is done - pass all relevant information for results activity to display.
        // Passing manager just in case for future statistics if needbe.
        Intent intent = new Intent(BridgeActivity.this, ResultsActivity.class);
        intent.putExtra("manager", manager);
        intent.putExtra("players", manager.players);
        intent.putExtra("scores", new int[]{manager.players[0].score, manager.players[1].score,
                manager.players[2].score, manager.players[3].score});
        startActivity(intent);
        finish();
    }

    // reshuffles deck, increments round count, resets all variables for the next round.
    public void reset() {
        manager.reset();
        manager.addedGuesses = 0;
    }

    // Executes AI moves for the next player onwards, stopping once we're on a player that isn't a bot.
    // This mutates currentPlayerInteracting (to the next non-AI player or player whose hand is empty) and the pot as it loops.
    public void executeAITurns() {
        int offset = 0;
        for(; offset < 4; offset++) {
            final int currentPlayer = (currentPlayerInteracting + offset) % manager.playerCount;
            //If the pot is already full, then we break and reset the manager (which will call this again to proceed through the AI).
            if(manager.pot.size() == 4)
                break;

            //If the pot position for this player is empty, then they haven't gone yet.
            //If the player is a bot, commence AI movement and go to the next player; if they aren't a bot, break and leave at this player.
            if(manager.pot.get(currentPlayer) == null) {
                if (isBot[currentPlayer] && manager.players[currentPlayer].hand.size() > 0) {
                    displayHands(currentPlayer);

                    Card bestMove = BridgeAI.chooseMove(currentPlayer, (BridgeManager) manager, levelsToSearch);
                    int chosenAI = manager.players[currentPlayer].hand.indexOf(bestMove);
                    manager.potHandle(chosenAI, currentPlayer);
                    for (int j = 0; j < 4; j++)
                        potClear();
                    displayPot();
                    displayHands(currentPlayer);
                } else
                    break;
            }
        }
        currentPlayerInteracting = (currentPlayerInteracting + offset) % manager.playerCount;
    }

    //Call when the end piles and the scores displayed on top of the piles need be redisplayed.
    public void displayEndPiles(List<Integer> scores) {
        TextView[] scoreViews = new TextView[] {(TextView)findViewById(R.id.bottomScore), (TextView)findViewById(R.id.leftScore),
                (TextView)findViewById(R.id.topScore), (TextView)findViewById(R.id.rightScore)};
        ImageView[] pileViews = new ImageView[] {(ImageView)findViewById(R.id.bottomPile), (ImageView)findViewById(R.id.leftPile),
                (ImageView)findViewById(R.id.topPile), (ImageView)findViewById(R.id.rightPile)};

        for(int i = 0; i < 4; i++) {
            //Update the score, but remove or update the pile if it exists.
            if (((BridgePlayer)manager.getPlayers()[i]).obtained > 0) {
                pileViews[i].setImageResource(getResources().getIdentifier("cardback", "drawable", getPackageName()));
                scoreViews[i].setText(Integer.toString(((BridgePlayer)manager.getPlayers()[i]).obtained));
                scoreViews[i].setVisibility(View.VISIBLE);
            } else {
                pileViews[i].setImageResource(0);
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

        if (currentPlayer == (manager.startPlayer + 3) % 4) {
            for (int i = 0; i <= manager.potsFinished; i++) {
                Button guessButton = new Button(this);
                guessButton.setText(Integer.toString(i));
                if (i != manager.getPlayers()[currentPlayer].hand.size() - manager.addedGuesses) {
                    guessButton.setBackgroundResource(R.drawable.guess_selectable);
                    guessButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            guess = Integer.parseInt(((TextView) v).getText().toString());
                        }
                    }); }
                else {
                    guessButton.setClickable(false);
                    guessButton.setBackgroundResource(R.drawable.guess_nonselectable);
                }
                layout.addView(guessButton);
            }
        } else {
            for (int j = 0; j <= manager.potsFinished; j++) {
                Button guessButton = new Button(this);
                guessButton.setText(Integer.toString(j));
                guessButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        guess = Integer.parseInt(((TextView) v).getText().toString());
                    }
                });
                layout.addView(guessButton);
            }
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
                            manager.addedGuesses += guess;
                            ((BridgePlayer) manager.players[currentPlayer]).guess = guess;

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
            manager.players[i].organize();
            //The coordinate and angular offsets for every card. Theta is dependent on the number of cards in the hand.
            int deltaX = 0, deltaY;
            float initialTheta= (float)-4.6*manager.getPlayers()[i].hand.size()/2;
            for(int j = 0; j < manager.getPlayers()[i].hand.size(); j++) {
                float theta = (float)(initialTheta + 4.6*j);
                deltaY = (int)(2.5*(30 - Math.pow(j - manager.getPlayers()[i].hand.size()/2,2))); //Truncate the result of the offset

                if(manager.getPlayers()[i].hand.size() % 2 != 0 && j == (manager.getPlayers()[i].hand.size()-1)/2)
                    theta = 0;

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
        getMenuInflater().inflate(R.menu.menu_bridge, menu);
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
