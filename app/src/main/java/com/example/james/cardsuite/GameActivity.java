package com.example.james.cardsuite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.SoundPool;
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
import java.util.Random;


public class GameActivity extends Activity {
    private Manager manager;
    private int currentPlayerInteracting = 0, currentPotTurn = 0, guessIndex = 0, potIndex = 0, guess = -1, gameMode = 0, soundsLoaded = 0;
    private boolean foundStartPlayer = false, buttonsPresent = false, finishedSwapping = false, finishedLoading = false;
    public boolean initialOutputWritten = false;
    private List<List<Card>> chosenLists = new ArrayList<List<Card>>();
    private List<Integer> chosenIndices = new ArrayList<Integer>();
    private List<Integer> scores = new ArrayList<Integer>(), roundScores = new ArrayList<>();
    private SoundPool[] soundPools = new SoundPool[] {new SoundPool.Builder().build(), new SoundPool.Builder().build(),
            new SoundPool.Builder().build(), new SoundPool.Builder().build()};
    private int[] sounds;
    private SoundPool.OnLoadCompleteListener loadListener;
    private Random r = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        gameMode = intent.getIntExtra("gameMode", 0);

        //Play the sound of a card being played, unless it's hearts wherein it might be bid for swapping (different sound used).
        sounds = new int[] {soundPools[0].load(this,R.raw.cardplace1,1), soundPools[1].load(this,R.raw.cardplace2,1),
                soundPools[2].load(this,R.raw.cardplace3,1), soundPools[3].load(this,R.raw.swapcardselect,1)};
        loadListener = new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundsLoaded++;
            }
        };

        for(SoundPool pool : soundPools)
            pool.setOnLoadCompleteListener(loadListener);

        for (int i = 0; i < 4; i++) {
            scores.add(0);
        }

        if (gameMode == 1) {
            manager = new HeartsManager();

            //Display the image buttons
            displayHands(0);
            displayEndPiles(scores, gameMode);
        }
        else if (gameMode == 2) {
            manager = new BridgeManager();
            manager.totalRoundCount = 12; // Change later for variable number of players

            ImageView trumpView = (ImageView)findViewById(R.id.trumpView);
            trumpView.setVisibility(View.VISIBLE);
            Card trumpCard = ((BridgeManager)manager).trumpCard;
            trumpView.setImageResource(getResources().getIdentifier(trumpCard.getAddress(), "drawable", getPackageName()));

            //Display the image buttons
            displayEndPiles(scores, gameMode);

            for(int i = 3; i >= 0; i--)
                openGuessDialog(i, gameMode);

            currentPlayerInteracting = manager.findStartPlayer();
            displayHands(manager.findStartPlayer());
        }
        else if (gameMode == 3) {
            manager = new SpadesManager();

            //Display the image buttons
            displayHands(0);
            displayEndPiles(scores, gameMode);

            for(int i = 3; i >= 0; i--)
                openGuessDialog(i, gameMode);

            currentPlayerInteracting = manager.findStartPlayer();
            displayHands(manager.findStartPlayer());
        }

        displayEndPiles(scores, gameMode);
    }

    public void menuClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Holo_Dialog_MinWidth);
        builder.setTitle("Are you sure you want to exit the game?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    //Processes the state of the game manager for hearts.
    public void gameClick(View v) {
        //If all sounds loaded, set the flag to true.
        if(soundsLoaded == sounds.length)
            finishedLoading = true;

        //Play sounds only if we're done swapping in hearts or are in any other game mode.
        if((finishedLoading && (gameMode == 1 && finishedSwapping)) || (finishedLoading && gameMode != 1)) {
            int chosenSound = r.nextInt(3);
            soundPools[chosenSound].play(sounds[chosenSound],1,1,0,0,1);
        }

        switch(gameMode) {
            case 1: this.heartsHandle(v); break;
            case 2: this.bridgeHandle(v); break;
            case 3: this.spadesHandle(v); break;
        }
    }

    //Processes the state of the game manager for hearts.
    public void bridgeHandle(View v) {
        int chosen = v.getId();
        for(int i = 0; i < currentPlayerInteracting; i++)
            chosen -= manager.getPlayers()[i].hand.size();

        // players start putting cards into the pot and calculate score
        if(manager.potsFinished < manager.totalRoundCount - 1) {
            manager.potHandle(chosen,currentPlayerInteracting, false, this);
            displayPot();

            currentPlayerInteracting = (currentPlayerInteracting+1) % manager.playerCount;
            potIndex++;
            displayHands(currentPlayerInteracting);

            //Set up the next round, reset all variables.
            if(manager.getPlayers()[currentPlayerInteracting].hand.isEmpty()) {
                potIndex = 0;

                manager.potAnalyze();
                scores.clear();
                for (Player player : manager.players) {
                    player.scoreChange();
                    scores.add(player.score);
                }

                // resets deck, hands, etc. and increments round
                manager.reset();
                manager.addedGuesses = 0;
                for (int i = 0; i < 4; i++)
                    potClear();
                displayPot();
                displayEndPiles(scores,gameMode);
                currentPlayerInteracting = (currentPlayerInteracting + 1) % 4;
                openGuessDialog(currentPlayerInteracting,gameMode);
                displayHands(manager.findStartPlayer());

                //Redisplay the trump
                ImageView trumpView = (ImageView)findViewById(R.id.trumpView);
                Card trumpCard = ((BridgeManager)manager).trumpCard;
                trumpView.setImageResource(getResources().getIdentifier(trumpCard.getAddress(), "drawable", getPackageName()));
            }
            return;
        }
        // The game is done - pass all relevant information for results activity to display.
        // Passing manager just in case for future statistics if needbe.
        Intent intent = new Intent(GameActivity.this, ResultsActivity.class);
        intent.putExtra("manager", manager);
        intent.putExtra("players", manager.players);
        startActivity(intent);
        finish();
    }

    public void spadesHandle(View v) {
        if(!(manager.isGameOver())) {
            int chosen = v.getId();
            for (int i = 0; i < currentPlayerInteracting; i++)
                chosen -= manager.getPlayers()[i].hand.size();

            if (!foundStartPlayer) {
                currentPlayerInteracting = manager.findStartPlayer();
                foundStartPlayer = true;
            }

            if (manager.potHandle(chosen, currentPlayerInteracting, initialOutputWritten, this)) {
                if (currentPlayerInteracting == manager.startPlayer)
                    for (int i = 0; i < 4; i++)
                        potClear();

                displayPot();
                currentPlayerInteracting = (currentPlayerInteracting + 1) % 4;

                // If the pot reaches max size of 4, then we know to continue and compare cards
                if (manager.pot.size() != 4) {
                    return;
                }
            } else {
                return;
            }

            if (currentPlayerInteracting == manager.startPlayer && currentPotTurn != 13) {
                currentPotTurn++;

                manager.potAnalyze(); //sets the new start player for the next pot
                currentPlayerInteracting = manager.startPlayer;
                manager.players[currentPlayerInteracting].handsWon++;

                displayEndPiles(scores,gameMode);
                manager.pot.clear();
                manager.newRound();

                if (currentPotTurn != 13) {
                    displayHands(manager.startPlayer);
                    return;
                }
            }

            if(currentPotTurn == 13 && !manager.isGameOver()) {
                List<Integer> scores = new ArrayList<Integer>();
                for (Player player : manager.getPlayers()) {
                    player.scoreChange();
                    scores.add(player.score);
                }

                foundStartPlayer = false;
                displayEndPiles(scores, gameMode);
                reset();
                openGuessDialog(currentPlayerInteracting,gameMode);
                return;
            }
        } else {
            // The game is done - pass all relevant information for results activity to display.
            // Passing manager just in case for future statistics if needbe.
            Intent intent = new Intent(GameActivity.this, ResultsActivity.class);
            intent.putExtra("manager", manager);
            intent.putExtra("players", manager.getPlayers());
            startActivity(intent);
        }
        finish();
    }

    public void heartsHandle(View v) {
        if(!(manager.isGameOver())) {
            int chosen = v.getId();
            for(int i = 0; i < currentPlayerInteracting; i++)
                chosen -= manager.getPlayers()[i].hand.size();

            Card chosenCard = manager.getPlayers()[currentPlayerInteracting].hand.get(chosen);

            //Select or unselect the card with a border shown
            if(chosenCard.isClicked == false) {
                v.setBackgroundResource(R.drawable.card_border);
                chosenCard.isClicked = true;
            } else {
                v.setBackgroundResource(0);
                chosenCard.isClicked = false;
            }

            if(!finishedSwapping) {
                //Play swapping sound.
                soundPools[3].play(sounds[3],1,1,0,0,1);

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
                    chosenIndices = new ArrayList<Integer>(); //restart the indices chosen for the next player

                    if (currentPlayerInteracting == 4) {
                        for (int i = 0; i < 4; i++) {
                            manager.swapCards(chosenLists.get(i), i, swapRound);
                            manager.getPlayers()[i].organize();
                            displayHands(0);
                            currentPlayerInteracting = 0;
                        }
                        finishedSwapping = true;
                    } else {
                        manager.getPlayers()[currentPlayerInteracting].organize();
                        displayHands(currentPlayerInteracting);
                        return;
                    }
                }
            }

            // Done swapping by this point - this should only be called once, the turn directly when we're done swapping.
            // Part 2 - Find player with 2 of clubs, we do this here because the card may be swapped.
            if(!foundStartPlayer) {
                currentPlayerInteracting = manager.findStartPlayer();
                foundStartPlayer = true;
            }

            // Part 3 - handle cards being tossed in the pot until all cards are gone (13 turns).
            if(manager.potHandle(chosen, currentPlayerInteracting, initialOutputWritten, this)) {
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
                for (Card c : manager.pot.values())
                    ((HeartsPlayer) manager.getPlayers()[manager.startPlayer]).endPile.add(c);

                roundScores.clear();
                for (int i = 0; i < manager.getPlayers().length; i++)
                    roundScores.add(((HeartsPlayer)manager.getPlayers()[i]).tallyRoundScore());

                displayEndPiles(roundScores,gameMode);
                manager.pot.clear();
                manager.newRound();

                if (currentPotTurn != 13) {
                    displayHands(manager.startPlayer);
                    return;
                }
            }

            // Part 4 - The round is done, update the score and reset the manager for the next round.
            if(currentPotTurn == 13 && !manager.isGameOver()) {
                scores.clear();
                boolean shootMoon = false;
                for (Player player : manager.getPlayers()) {
                    shootMoon = ((HeartsPlayer) player).scoreChange();
                    if (shootMoon) {
                        for (Player otherPlayers : manager.getPlayers()) {
                            if (!otherPlayers.equals(player)) {
                                otherPlayers.score += 26;
                            }
                        }
                    }
                    scores.add(player.score);
                }
                roundScores.clear();
                for (int i = 0; i < manager.getPlayers().length; i++)
                    roundScores.add(((HeartsPlayer)manager.getPlayers()[i]).tallyRoundScore());
                displayEndPiles(roundScores,gameMode);
                reset();
                return;
            }
        } else {
            // The game is done - pass all relevant information for results activity to display.
            // Passing manager just in case for future statistics if needbe.
            Intent intent = new Intent(GameActivity.this, ResultsActivity.class);
            intent.putExtra("manager", manager);
            intent.putExtra("players", manager.getPlayers());
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

    //Call when the end piles and the scores displayed on top of the piles need be redisplayed.
    public void displayEndPiles(List<Integer> scores, int gameMode) {
        TextView[] scoreViews = new TextView[] {(TextView)findViewById(R.id.bottomScore), (TextView)findViewById(R.id.leftScore),
                (TextView)findViewById(R.id.topScore), (TextView)findViewById(R.id.rightScore)};
        ImageView[] pileViews = new ImageView[] {(ImageView)findViewById(R.id.bottomPile), (ImageView)findViewById(R.id.leftPile),
                (ImageView)findViewById(R.id.topPile), (ImageView)findViewById(R.id.rightPile)};

        for(int i = 0; i < 4; i++) {
            //Update the score, but remove or update the pile if it exists.
            if(gameMode == 1 && scores.get(i) != 0) {
                pileViews[i].setImageResource(getResources().getIdentifier("cardback", "drawable", getPackageName()));
                scoreViews[i].setVisibility(View.VISIBLE);
                scoreViews[i].setText(Integer.toString(scores.get(i)));
            } else {
                pileViews[i].setImageResource(0);
                scoreViews[i].setVisibility(View.INVISIBLE);
            }

            if ((manager.getPlayers()[i].handsWon) > 0 && gameMode != 1) {
                scoreViews[i].setText(Integer.toString(manager.getPlayers()[i].handsWon));
                scoreViews[i].setVisibility(View.VISIBLE);
            } else if(gameMode != 1){
                scoreViews[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    //Opens the guess dialog - fit for German Bridge for now.
    public void openGuessDialog(final int currentPlayer, final int gameMode) {
        displayHands(currentPlayer);

        AlertDialog.Builder builder = new AlertDialog.Builder(this,android.R.style.Theme_Holo_Panel);
        builder.setCancelable(false);
        builder.setTitle("Player " + (currentPlayer + 1) + " bids");
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        if (gameMode == 3) {
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
        } else {
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
                            if (gameMode == 2) {
                                manager.addedGuesses += guess;
                                ((BridgePlayer) manager.players[currentPlayer]).guess = guess;
                            } else if (gameMode == 3)
                                ((SpadesPlayer) manager.players[currentPlayer]).bid = guess;

                            d.dismiss();
                        }
                    }
                });
            }
        });
        d.show();
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
