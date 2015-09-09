package com.example.james.cardsuite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private int guess = -1, guessCount = 0, botCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spades);

        Intent intent = getIntent();
        boolean loadGame = intent.getBooleanExtra("loadGame", false);

        if (loadGame) {
            this.loadGame();
            displayPot();
            displayEndPiles(scores);
            displayHands(currentPlayerInteracting, true);
        } else {
            this.isBot = intent.getBooleanArrayExtra("isBot");

            for (int i = 0; i < 4; i++)
                if (isBot[i])
                    botCount++;

            //Find the first and last nonbot players for later ease of use.
            for (int i = 0; i < 4; i++) {
                if (!foundFirstNonBot && !isBot[i]) {
                    firstNonBot = i;
                    foundFirstNonBot = true;
                } else if (!isBot[i]) {
                    lastNonBot = i;
                }
            }

            manager = new SpadesManager(currentPlayerInteracting);
            manager.totalRoundCount = 12; // Change later for variable number of players

            //Display the image buttons
            displayEndPiles(scores);

            //Artificial delay added so that this runs after onCreate finishes and the views' coordinates are defined.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dealCards();
                }
            }, 500);
        }
    }

    @Override
    public void onPause() {
        saveGame();
        super.onPause();
    }

    public void gameClick(View v) {
        //Prevents spam-clicking before the last button click is done.
        if (SystemClock.elapsedRealtime() - lastClickTime < 1750) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        super.gameClick(v);
        //Play sounds only if we're done swapping in hearts or are in any other game mode.
        if (finishedLoading) {
            int chosenSound = r.nextInt(3);
            soundPools[chosenSound].play(sounds[chosenSound], 1, 1, 0, 0, 1);
        }
        if (!(manager.isGameOver())) {
            int chosen = getCardIndex(v);

            manager.potHandle(chosen, currentPlayerInteracting);
            GameAnimation.placeCard(SpadesActivity.this, v, new Runnable() {
                @Override
                public void run() {
                    potClear();
                    displayPot();

                    executeAITurns();

                    updateGameState();
                }
            }, currentPlayerInteracting);
        }
    }

    // Updates the game state; after the player moves, the code cycles between executing AI turns/updating the game state until done.
    public void updateGameState() {
        final int lastPlayer = manager.startPlayer == 0 ? 3 : manager.startPlayer - 1;
        //If the pot is full (all players have tossed a card), reset the pot, analyze it, find the new start player/winner of the pot.
        if (manager.pot.size() == 4) {
            manager.potAnalyze();
            currentPlayerInteracting = manager.startPlayer;

            GameAnimation.collectEndPile(SpadesActivity.this, new Runnable() {
                @Override
                public void run() {
                    manager.pot = new HashMap<Integer, Card>();
                    potClear();
                    displayPot();
                    displayEndPiles(scores);

                    if (!manager.getPlayers()[lastPlayer].hand.isEmpty())
                        executeAITurns();
                    else if (!manager.isGameOver()) {
                        List<Integer> scores = new ArrayList<Integer>();
                        for (Player player : manager.getPlayers()) {
                            player.scoreChange();
                            scores.add(player.score);
                        }

                        reset();

                        final List<Integer> finalScores = scores;
                        displayScoreTable(new Runnable() {
                            @Override
                            public void run() {
                                displayEndPiles(finalScores);
                                dealCards();
                            }
                        });
                    }
                }
            }, currentPlayerInteracting);
        }

        if (manager.isGameOver()) {
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
    }

    // reshuffles deck, increments round count, resets all variables for the next round.
    public void reset() {
        manager.reset();
        finishedSwapping = false;
        buttonsPresent = false;
        currentPotTurn = 0;
        currentPlayerInteracting = 0;

        //currentPlayerInteracting default-init'd to 0, we increment until we find a non-bot player.
        while (isBot[currentPlayerInteracting]) {
            currentPlayerInteracting++;
        }
    }

    // Executes AI moves for the next player onwards, stopping once we're on a player that isn't a bot or the pot is already full.
    // This mutates currentPlayerInteracting (to the next non-AI player or player whose hand is empty) and the pot as it loops.
    public void executeAITurns() {
        long currentTimeDelay = 250;
        int offset = 0;
        for (; offset < 4; offset++) {
            final int currentPlayer = (currentPlayerInteracting + offset) % manager.playerCount;
            //If the pot is already full, then we break and reset the manager (which will call this again to proceed through the AI).
            if (manager.pot.size() == 4)
                break;

            //If the pot position for this player is empty, then they haven't gone yet.
            //If the player is a bot, commence AI movement and go to the next player; if they aren't a bot, break and leave at this player.
            if (manager.pot.get(currentPlayer) == null) {
                if (isBot[currentPlayer] && manager.players[currentPlayer].hand.size() > 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //After the delay, proceed the AI move.
                            Card bestMove = SpadesAI.chooseMove(currentPlayer, (SpadesManager) manager, levelsToSearch);

                            int chosenAI = manager.players[currentPlayer].hand.indexOf(bestMove);

                            manager.potHandle(chosenAI, currentPlayer);

                            ImageView cardView = (ImageView) findViewByCard(bestMove);
                            GameAnimation.placeCard(SpadesActivity.this, cardView, null, currentPlayer);

                            int chosenSound = r.nextInt(3);
                            soundPools[chosenSound].play(sounds[chosenSound], 1, 1, 0, 0, 1);

                            potClear();
                            displayPot();
                            if (!manager.players[currentPlayerInteracting].isBot)
                                displayHands(currentPlayerInteracting, true);
                        }
                    }, currentTimeDelay);
                } else
                    break;

                currentTimeDelay += 250;
            }
        }
        currentPlayerInteracting = (currentPlayerInteracting + offset) % manager.playerCount;

        //The last non-bot player to display, since currentPlayerInteracting will be reset to the new start player.
        //TODO - Handler exists only to update game state after all AI is done - can be eliminated if offset is calculated before.
        final int playerToDisplay = currentPlayerInteracting;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateGameState();
                if (!manager.players[currentPlayerInteracting].isBot)
                    displayHands(playerToDisplay, true);
            }
        }, currentTimeDelay + 250);
    }

    //Call when the end piles and the scores displayed on top of the piles need be redisplayed.
    public void displayEndPiles(List<Integer> scores) {
        TextView[] scoreViews = new TextView[]{(TextView) findViewById(R.id.bottomScore), (TextView) findViewById(R.id.leftScore),
                (TextView) findViewById(R.id.topScore), (TextView) findViewById(R.id.rightScore)};
        ImageView[] pileViews = new ImageView[]{(ImageView) findViewById(R.id.bottomPile), (ImageView) findViewById(R.id.leftPile),
                (ImageView) findViewById(R.id.topPile), (ImageView) findViewById(R.id.rightPile)};

        for (int i = 0; i < 4; i++) {
            //Update the score, but remove or update the pile if it exists.
            pileViews[i].setMaxHeight(150);
            pileViews[i].setAdjustViewBounds(true);
            if ((((SpadesPlayer) manager.getPlayers()[i]).obtained) > 0) {
                pileViews[i].setImageResource(getResources().getIdentifier("cardback", "drawable", getPackageName()));
                scoreViews[i].setText(Integer.toString(((SpadesPlayer) manager.getPlayers()[i]).obtained));
                scoreViews[i].setVisibility(View.VISIBLE);
            } else {
                pileViews[i].setImageResource(0);
                scoreViews[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    public void openGuessDialog(final int currentPlayer) {
        displayHands(currentPlayer, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        guess = -1;

        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.HORIZONTAL);

        TextView text = new TextView(this);
        text.setText("Player " + (currentPlayer + 1) + " bid");
        text.setTextSize(16);
        text.setPadding(5, 5, 5, 5);
        text.setTypeface(null, Typeface.BOLD);
        builder.setCustomTitle(text);
        Button[] buttons = new Button[14];

        for (int i = 0; i <= 13; i++) {
            final RadioButton button = new RadioButton(this);
            button.setText(Integer.toString(i));
            button.setTag(i);
            buttons[i] = button;
            group.addView(button);
        }

        layout.addView(group);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        horizontalScrollView.addView(layout);
        horizontalScrollView.setLayoutParams(new AbsListView.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        builder.setView(horizontalScrollView);
        final AlertDialog d = builder.create();

        for (final Button button : buttons) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (guess == (Integer) button.getTag()) {
                        manager.addedGuesses += guess;
                        ((SpadesPlayer) manager.players[currentPlayer]).bid = guess;

                        d.dismiss();
                        guessCount++;
                        if (guessCount == 4) {
                            currentPlayerInteracting = manager.findStartPlayer();
                            ((SpadesManager) manager).addBids();
                            executeAITurns();
                            guessCount = 0;
                            return;
                        }
                        int player = (currentPlayer + 1) % 4;
                        while (isBot[player]) {
                            ((SpadesPlayer) manager.getPlayers()[player]).bid = SpadesAI.getBid(player, (SpadesManager) manager);
                            guessCount++;
                            if (guessCount == 4) {
                                currentPlayerInteracting = manager.findStartPlayer();
                                ((SpadesManager) manager).addBids();
                                executeAITurns();
                                guessCount = 0;
                                return;
                            }
                            player = (player + 1) % 4;
                        }
                        openGuessDialog(player);
                    } else {
                        guess = (Integer) button.getTag();
                    }
                }
            });
        }

        d.getWindow().setLayout(findViewById(R.id.potLayout).getWidth(), findViewById(R.id.potLayout).getHeight());
        d.show();
    }

    //Call when the hands have been updated and need be redisplayed.
    public void displayHands(int player, boolean cardsClickable) {
        //Remove all old cards first
        cardViews = new ArrayList<ImageView>();
        if (buttonsPresent) {
            for (int i = 0; i < 52; i++) {
                View view = findViewById(i);
                if (view != null)
                    ((ViewGroup) view.getParent()).removeView(view);
            }
        }

        int temporaryID = 0; //Temporary ID to be assigned to each card, to be reused.
        RelativeLayout left = (RelativeLayout) findViewById(R.id.leftPlayerHandLayout),
                top = (RelativeLayout) findViewById(R.id.topPlayerHandLayout),
                right = (RelativeLayout) findViewById(R.id.rightPlayerHandLayout),
                bottom = (RelativeLayout) findViewById(R.id.bottomPlayerHandLayout);

        //Now create the imagebuttons for each of the players.
        //Note: other possible param values for initialTheta scalar and deltaY scalar are (5,3).
        for (int i = 0; i < 4; i++) {
            manager.players[i].organize();
            //The coordinate and angular offsets for every card. Theta is dependent on the number of cards in the hand.
            int deltaX = 0, deltaY;
            float initialTheta = (float) -2.25 * manager.getPlayers()[i].hand.size() / 2;
            for (int j = 0; j < manager.getPlayers()[i].hand.size(); j++) {
                float theta = (float) (initialTheta + 2.25 * j);
                deltaY = (int) (1.2 * (17.5 - Math.pow(j - manager.getPlayers()[i].hand.size() / 2, 2))); //Truncate the result of the offset

                if (manager.getPlayers()[i].hand.size() % 2 != 0 && j == (manager.getPlayers()[i].hand.size() - 1) / 2)
                    theta = 0;

                RelativeLayout.LayoutParams restParams = new RelativeLayout.LayoutParams(cardWidthPX, cardHeightPX);

                //How to treat and initialize the other cards depending on whether the current player or any other.
                ImageView cardButton;
                if (i == player) {
                    cardButton = new ImageButton(this);
                    cardButton.setImageResource(getResources().getIdentifier(manager.getPlayers()[player].hand.get(j).getAddress(), "drawable", getPackageName()));
                    cardButton.setBackgroundResource(R.drawable.card_border);
                    cardButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            gameClick(v);
                        }
                    });

                    //Tint and make the card unselectable if it's not meant to be.
                    Card selectCard = manager.getPlayers()[i].hand.get(j);
                    if (!(manager.cardSelectable(selectCard, finishedSwapping, i))) {
                        cardButton.setColorFilter(Color.parseColor("#78505050"), PorterDuff.Mode.SRC_ATOP);
                        cardButton.setClickable(false);
                    }
                } else {
                    cardButton = new ImageView(this);
                    cardButton.setImageResource(getResources().getIdentifier("cardback", "drawable", getPackageName()));
                }

                cardButton.setTag(manager.getPlayers()[i].hand.get(j));
                cardButton.setPadding(1, 1, 1, 1);
                if (!cardsClickable)
                    cardButton.setClickable(false);
                cardButton.setMaxHeight(150);
                cardButton.setScaleType(ImageView.ScaleType.FIT_CENTER);

                switch (i) {
                    case 0:
                        restParams.setMargins(deltaX, 65 - deltaY, 0, 0);
                        cardButton.setRotation(theta);
                        bottom.addView(cardButton, restParams);
                        break;
                    case 1:
                        restParams.setMargins(100 + deltaY, deltaX, 0, 0);
                        cardButton.setRotation(90 + theta);
                        left.addView(cardButton, restParams);
                        break;
                    case 2:
                        restParams.setMargins(deltaX, deltaY, 0, 0);
                        cardButton.setRotation(180 - theta);
                        top.addView(cardButton, restParams); break;
                    case 3:
                        restParams.setMargins(110 - deltaY, deltaX, 0, 0);
                        cardButton.setRotation(270 - theta);
                        right.addView(cardButton, restParams);
                        break;
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

    public void dealCards() {
        long currentTimeDelay = 0;
        final int[] initialCoordinates = new int[2];
        findViewById(R.id.anchor).getLocationOnScreen(initialCoordinates);
        final int originalHandSize = manager.players[0].hand.size();
        for (int j = 0; j < manager.players[0].hand.size(); j++) {
            final int cardsDisplayed = j;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    soundPools[4].play(sounds[4], 1, 1, 0, 0, 1);
                    GameAnimation.dealSingleCards(SpadesActivity.this, new Runnable() {
                        @Override
                        public void run() {
                            displayIntermediateHands(cardsDisplayed);

                            if (cardsDisplayed == originalHandSize - 1) {
                                int player = manager.findStartPlayer();
                                while (isBot[player]) {
                                    ((SpadesPlayer) manager.getPlayers()[player]).bid = SpadesAI.getBid(player, (SpadesManager) manager);
                                    guessCount++;
                                    player = (player + 1) % 4;
                                }
                                openGuessDialog(player);
                            }
                        }
                    }, initialCoordinates);
                }
            }, currentTimeDelay);

            currentTimeDelay += 75;
        }
    }

    public void loadGame() {

        try {
            FileInputStream fis = this.openFileInput("save_spades");
            ObjectInputStream is = new ObjectInputStream(fis);
            super.loadGame(is);
            this.manager = (SpadesManager) is.readObject();
            this.botCount = is.readInt();
            this.guessCount = is.readInt();
            this.guess = is.readInt();
            is.close();
            fis.close();
            deleteFile("save_spades");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveGame() {
        String filename = "save_spades";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
            super.saveGame(objectStream);
            objectStream.writeObject(this.manager);
            objectStream.writeInt(botCount);
            objectStream.writeInt(guessCount);
            objectStream.writeInt(guess);

            objectStream.close();
            outputStream.close();
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
