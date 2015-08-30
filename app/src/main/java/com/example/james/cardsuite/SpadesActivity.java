package com.example.james.cardsuite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpadesActivity extends GameActivity {
    private int guess = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spades);

        Intent intent = getIntent();
        boolean loadGame = intent.getBooleanExtra("loadGame", false);

        if (loadGame) {
            this.loadGame();
            displayPot();
        }
        else {
            this.isBot = intent.getBooleanArrayExtra("isBot");
            //currentPlayerInteracting default-init'd to 0, we increment until we find a non-bot player.
            while (isBot[currentPlayerInteracting]) {
                currentPlayerInteracting++;
            }

            manager = new SpadesManager(currentPlayerInteracting);
            manager.totalRoundCount = 12; // Change later for variable number of players

            for (int i = 3; i >= 0; i--) {
                if (isBot[i])
                    ((SpadesPlayer) (manager.players[i])).bid = SpadesAI.getBid(i, (SpadesManager) manager);
                else
                    openGuessDialog(i);
            }
        }
        //Display the image buttons
        displayEndPiles(scores);
        displayHands(currentPlayerInteracting, true);
    }

    @Override
    public void onPause() {
        saveGame();
        super.onPause();
    }

    public void gameClick(View v) {
        //Prevents spam-clicking before the last button click is done.
        if (SystemClock.elapsedRealtime() - lastClickTime < 1750){
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        super.gameClick(v);
        //Play sounds only if we're done swapping in hearts or are in any other game mode.
        if(finishedLoading) {
            int chosenSound = r.nextInt(3);
            soundPools[chosenSound].play(sounds[chosenSound], 1, 1, 0, 0, 1);
        }
        if(!(manager.isGameOver())) {
            int chosen = getCardIndex(v);

            manager.potHandle(chosen, currentPlayerInteracting);
            GameAnimation.placeCard(SpadesActivity.this,v,currentPlayerInteracting);

            potClear();
            displayPot();

            executeAITurns();

            updateGameState();
        }
    }

    public void updateGameState() {
        //If the pot is full (all players have tossed a card), reset the pot, analyze it, find the new start player/winner of the pot.
        if(manager.pot.size() == 4) {
            manager.potAnalyze();
            currentPlayerInteracting = manager.startPlayer;
            GameAnimation.collectEndPile(SpadesActivity.this, currentPlayerInteracting);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    manager.pot = new HashMap<Integer, Card>();
                    potClear();
                    displayPot();
                    displayEndPiles(scores);

                    executeAITurns();
                }
            },250);
            return;
        }

        //If all the hands are exhausted, restart the entire game (until a score has reached 500).
        int lastPlayer = manager.startPlayer == 0 ? 3 : manager.startPlayer - 1;
        if(manager.getPlayers()[lastPlayer].hand.isEmpty() && !manager.isGameOver()) {
            List<Integer> scores = new ArrayList<Integer>();
            for (Player player : manager.getPlayers()) {
                player.scoreChange();
                scores.add(player.score);
            }

            reset();
            displayScoreTable();

            for(int i = 3; i >= 0; i--) {
                if(isBot[i])
                    ((SpadesPlayer) (manager.players[i])).bid = SpadesAI.getBid(i, (SpadesManager) manager);
                else
                    openGuessDialog(i);
            }

            //Display the image buttons
            displayEndPiles(scores);
            displayHands(currentPlayerInteracting, false);

            //Cycle through any AI players for the first non-AI player.
            executeAITurns();
            return;
        }

        if(!(manager.isGameOver()))
            return;

        // The game is done - pass all relevant information for results activity to display.
        // Passing manager just in case for future statistics if needbe.
        Intent intent = new Intent(SpadesActivity.this, ResultsActivity.class);
        intent.putExtra("manager", manager);
        intent.putExtra("players", manager.getPlayers());
        intent.putExtra("scores", new int[]{manager.players[0].score, manager.players[1].score,
                manager.players[2].score, manager.players[3].score});
        startActivity(intent);
        finish();
    }

    // reshuffles deck, increments round count, resets all variables for the next round.
    public void reset() {
        manager.reset();
        finishedSwapping = false; buttonsPresent = false;
        currentPotTurn = 0; currentPlayerInteracting = 0;

        //currentPlayerInteracting default-init'd to 0, we increment until we find a non-bot player.
        while(isBot[currentPlayerInteracting]) {
            currentPlayerInteracting++;
        }
    }

    // Executes AI moves for the next player onwards, stopping once we're on a player that isn't a bot.
    // This mutates currentPlayerInteracting (to the next non-AI player or player whose hand is empty) and the pot as it loops.
    public void executeAITurns() {
        long currentTimeDelay = 250;
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
                    final long timeDelay = currentTimeDelay;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //After the delay, proceed the AI move.
                            Card bestMove = SpadesAI.chooseMove(currentPlayer, (SpadesManager) manager, levelsToSearch);
                            int chosenAI = manager.players[currentPlayer].hand.indexOf(bestMove);
                            manager.potHandle(chosenAI, currentPlayer);

                            ImageView cardView = (ImageView)findViewByCard(bestMove);
                            GameAnimation.placeCard(SpadesActivity.this, cardView, currentPlayerInteracting);

                            int chosenSound = r.nextInt(3);
                            soundPools[chosenSound].play(sounds[chosenSound],1,1,0,0,1);

                            potClear();
                            displayPot();
                            displayHands(currentPlayerInteracting, false);
                        }
                    }, timeDelay);
                } else
                    break;

                currentTimeDelay += 250;
            }
        }
        currentPlayerInteracting = (currentPlayerInteracting + offset) % manager.playerCount;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateGameState();
                displayHands(currentPlayerInteracting, true);
            }
        }, currentTimeDelay);
    }

    //Call when the end piles and the scores displayed on top of the piles need be redisplayed.
    public void displayEndPiles(List<Integer> scores) {
        TextView[] scoreViews = new TextView[] {(TextView)findViewById(R.id.bottomScore), (TextView)findViewById(R.id.leftScore),
                (TextView)findViewById(R.id.topScore), (TextView)findViewById(R.id.rightScore)};
        ImageView[] pileViews = new ImageView[] {(ImageView)findViewById(R.id.bottomPile), (ImageView)findViewById(R.id.leftPile),
                (ImageView)findViewById(R.id.topPile), (ImageView)findViewById(R.id.rightPile)};

        for(int i = 0; i < 4; i++) {
            //Update the score, but remove or update the pile if it exists.
            pileViews[i].setMaxHeight(115);
            pileViews[i].setAdjustViewBounds(true);
            if ((((SpadesPlayer)manager.getPlayers()[i]).obtained) > 0) {
                pileViews[i].setImageResource(getResources().getIdentifier("cardback", "drawable", getPackageName()));
                scoreViews[i].setText(Integer.toString(((SpadesPlayer)manager.getPlayers()[i]).obtained));
                scoreViews[i].setVisibility(View.VISIBLE);
            } else {
                pileViews[i].setImageResource(0);
                scoreViews[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    //Opens the guess dialog - fit for German Bridge for now.
    public void openGuessDialog(final int currentPlayer) {
        displayHands(currentPlayer,true);
        guess = -1;

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
    public void displayHands(int player, boolean cardsClickable) {
        //Remove all old cards first
        cardViews = new ArrayList<ImageView>();
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

                cardButton.setTag(manager.getPlayers()[i].hand.get(j));
                cardButton.setPadding(3, 3, 3, 3);
                if(!cardsClickable)
                    cardButton.setClickable(false);
                cardButton.setMaxHeight(115);
                cardButton.setAdjustViewBounds(true);

                switch(i) {
                    case 0: restParams.setMargins(deltaX,95-deltaY,0,0);
                        cardButton.setRotation(theta);
                        bottom.addView(cardButton, restParams); break;
                    case 1: restParams.setMargins(115+deltaY,deltaX,0,0);
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
                cardViews.add(cardButton);

                //Set the deltaX/theta parameters for the next card/loop iteration.
                //Consequence of more space horizontally than vertically; set smaller distance between cards vertically.
                deltaX = (i % 2 == 0) ? deltaX + 60 : deltaX + 55;
            }
        }
        buttonsPresent = true;
    }

    public void loadGame() {

        try {
            FileInputStream fis = this.openFileInput("save_spades");
            ObjectInputStream is = new ObjectInputStream(fis);
            this.manager = (SpadesManager) is.readObject();
            this.currentPlayerInteracting = is.readInt();
            this.currentPotTurn = is.readInt();
            this.foundStartPlayer = is.readBoolean();
            this.finishedSwapping = is.readBoolean();
            this.buttonsPresent = is.readBoolean();
            this.initialOutputWritten = is.readBoolean();
            this.isBot = (boolean[]) is.readObject();
            this.scores = (List<Integer>) is.readObject();
            this.roundScores = (List<Integer>) is.readObject();
            is.close();
            fis.close();
            deleteFile("save_spades");
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public void saveGame() {
        String filename = "save_spades";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
            objectStream.writeObject(this.manager);
            objectStream.writeInt(currentPlayerInteracting);
            objectStream.writeInt(currentPotTurn);
            objectStream.writeBoolean(foundStartPlayer);
            objectStream.writeBoolean(finishedSwapping);
            objectStream.writeBoolean(buttonsPresent);
            objectStream.writeBoolean(initialOutputWritten);
            objectStream.writeObject(isBot);
            objectStream.writeObject(scores);
            objectStream.writeObject(roundScores);

            outputStream.close();
            objectStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
