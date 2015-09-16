package com.gtjgroup.cardsuite;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BridgeActivity extends GameActivity implements Serializable {
    private int guess = -1, guessCount = 0, botCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bridge);

        Intent intent = getIntent();
        isGameLoaded = intent.getBooleanExtra("loadGame", false);
        if (isGameLoaded) {
            this.loadGame();
            displayPot();
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

            manager = new BridgeManager(currentPlayerInteracting);
            manager.totalRoundCount = 12;
        }
        ImageView trumpView = (ImageView) findViewById(R.id.trumpView);
        trumpView.setVisibility(View.VISIBLE);
        Card trumpCard = ((BridgeManager) manager).trumpCard;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)trumpView.getLayoutParams();
        params.width = cardWidthPX;
        params.height = cardHeightPX;
        trumpView.setImageResource(getResources().getIdentifier(trumpCard.getAddress(), "drawable", getPackageName()));
        trumpView.setAdjustViewBounds(true);
        //Display the image buttons
        displayEndPiles(scores);

        View[] potViews = new View[] {findViewById(R.id.leftPotCard),findViewById(R.id.topPotCard),
                findViewById(R.id.rightPotCard),findViewById(R.id.bottomPotCard)};
        for(View card : potViews) {
            RelativeLayout.LayoutParams params5 = (RelativeLayout.LayoutParams)card.getLayoutParams();
            params5.width = cardWidthPX;
            params5.height = cardHeightPX;
        }

        //Artificial delay added so that this runs after onCreate finishes and the views' coordinates are defined.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dealCards();
            }
        }, 500);
    }

    @Override
    public void onPause() {
        this.saveGame();
        super.onPause();
    }

    public void gameClick(View v) {
        //Prevents spam-clicking before the last button click is done.
        if (!canClick && SystemClock.elapsedRealtime() - lastClickTime < 200)
            return;
        canClick = false;
        lastClickTime = SystemClock.elapsedRealtime();

        super.gameClick(v);
        //Play sounds only if we're done swapping in hearts or are in any other game mode.
        if (SoundManager.isLoaded())
            SoundManager.playPlaceCardSound();
        //Get the index of the chosen card in the current player's hand.
        int chosen = getCardIndex(v);

        // players start putting cards into the pot and calculate score
        if (manager.potsFinished <= manager.totalRoundCount) {
            manager.potHandle(chosen, currentPlayerInteracting);
            GameAnimation.placeCard(this, v, new Runnable() {
                @Override
                public void run() {
                    displayHands(currentPlayerInteracting, false, true);
                    potClear();
                    displayPot();
                    final int currentPotSize = manager.pot.size();
                    final int lastPlayer = manager.startPlayer == 0 ? 3 : manager.startPlayer - 1;
                    final int lastPlayerHandSize = manager.players[lastPlayer].hand.size();

                    currentPlayerInteracting = (currentPlayerInteracting + 1) % manager.playerCount;

                    updateGameState();

                    //If this is the last turn of the entire round, don't execute turns; wait for scoreboard.
                    if (currentPotSize != 4 && lastPlayerHandSize != 0) {
                        if (isBot[currentPlayerInteracting])
                            botHandle(250);
                        else {
                            if (botCount != 3) {
                                displayHands(-1, false, true);
                                displayWaitScreen(currentPlayerInteracting);
                            } else
                                displayHands(currentPlayerInteracting, true, true);
                            canClick = true;
                        }
                    }
                }
            }, currentPlayerInteracting);
        } else
            endGame();
    }

    // Executes AI moves for the next player onwards, stopping once we're on a player that isn't a bot.
    // This mutates currentPlayerInteracting (to the next non-AI player or player whose hand is empty) and the pot as it loops.
    public void botHandle(final long delay) {
        if (!manager.isGameOver()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Card bestMove = BridgeAI.chooseMove(currentPlayerInteracting, (BridgeManager) manager, levelsToSearch);
                    int chosenAI = manager.players[currentPlayerInteracting].hand.indexOf(bestMove);
                    manager.potHandle(chosenAI, currentPlayerInteracting);
                    final int currentPotSize = manager.pot.size();
                    final int lastPlayer = manager.startPlayer == 0 ? 3 : manager.startPlayer - 1;
                    final int lastPlayerHandSize = manager.players[lastPlayer].hand.size();

                    ImageView cardView = (ImageView) findViewByCard(bestMove);
                    GameAnimation.placeCard(BridgeActivity.this, cardView, new Runnable() {
                        @Override
                        public void run() {
                            potClear();
                            displayPot();

                            displayHands(lastNonBot, false, true);
                            SoundManager.playPlaceCardSound();

                            currentPlayerInteracting = (currentPlayerInteracting + 1) % manager.playerCount;

                            if (manager.pot.size() > 0)
                                updateGameState();

                            //If this is the last turn of the entire round, don't execute turns; wait for scoreboard.
                            if (currentPotSize != 4 && lastPlayerHandSize != 0) {
                                if (isBot[currentPlayerInteracting])
                                    botHandle(250);
                                else {
                                    if (botCount != 3) {
                                        displayHands(-1, false, true);
                                        displayWaitScreen(currentPlayerInteracting);
                                    } else
                                        displayHands(currentPlayerInteracting, true, true);
                                    canClick = true;
                                }
                            }
                        }
                    }, currentPlayerInteracting);
                }
            }, delay);
        } else
            endGame();
    }

    // Updates the game state; after the player moves, the code cycles between executing AI turns/updating the game state until done.
    public void updateGameState() {
        final int lastPlayer = manager.startPlayer == 0 ? 3 : manager.startPlayer - 1;
        //If the pot is full (all players have tossed a card), reset the pot, analyze it, find the new start player/winner of the pot.
        if (manager.pot.size() == 4) {
            manager.potAnalyze();
            currentPlayerInteracting = manager.startPlayer;

            GameAnimation.collectEndPile(BridgeActivity.this, new Runnable() {
                @Override
                public void run() {
                    manager.pot = new HashMap<Integer, Card>();
                    potClear();
                    displayPot();
                    displayEndPiles(scores);

                    //Finished round, restart it
                    if (manager.getPlayers()[lastPlayer].hand.isEmpty()) {
                        scores.clear();
                        for (Player player : manager.players) {
                            player.scoreChange();
                            scores.add(player.score);
                        }
                        manager.potsFinished++;
                        displayScoreTable(null);
                    } else {
                        if (isBot[currentPlayerInteracting])
                            botHandle(250);
                        else {
                            if (botCount != 3) {
                                displayHands(-1, false, true);
                                displayWaitScreen(currentPlayerInteracting);
                            }
                            else
                                displayHands(currentPlayerInteracting, true, true);
                            canClick = true;
                        }
                    }
                }
            }, currentPlayerInteracting);
        }
    }

    // reshuffles deck, increments round count, resets all variables for the next round.
    public void reset() {
        manager.reset();
        if (manager.isGameOver()) {
            endGame();
            return;
        }
        guessCount = 0;
        ((BridgeManager)manager).addedGuesses = 0;

        //Redisplay the trump
        ImageView trumpView = (ImageView) findViewById(R.id.trumpView);
        Card trumpCard = ((BridgeManager) manager).trumpCard;
        trumpView.setImageResource(getResources().getIdentifier(trumpCard.getAddress(), "drawable", getPackageName()));
        trumpView.setMaxHeight(150);
        trumpView.setAdjustViewBounds(true);
        displayEndPiles(scores);
        dealCards();
    }

    public void endGame() {
        // The game is done - pass all relevant information for results activity to display.
        // Passing manager just in case for future statistics if needbe.
        Intent intent = new Intent(BridgeActivity.this, ResultsActivity.class);
        intent.putExtra("manager", manager);
        intent.putExtra("players", manager.players);
        intent.putExtra("scores", new int[]{manager.players[0].score, manager.players[1].score,
                manager.players[2].score, manager.players[3].score});
        intent.putExtra("game_mode",1);
        startActivity(intent);
        finish();
    }

    public void displayScoreTable(Runnable closeAction) {
        String[] column = {"Player 1", "Player 2", "Player 3", "Player 4"};
        List<String> row = new ArrayList<>();
        for (int i = 1; i <= manager.getPotsFinished() - 1; i++)
            row.add("Round " + (i));

        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setLayoutParams(new LinearLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.MATCH_PARENT);
        rowParams.gravity = Gravity.CENTER_VERTICAL;
        rowParams.setMargins(10, 5, 10, 5);


        for (int i = 0; i <= row.size(); i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(rowParams);

            for (int j = 0; j <= column.length; j++) {

                TextView textView = new TextView(this);
                if (i == 0 && j == 0)
                    textView.setText("");
                else if (i == 0) {
                    textView.setText(column[j - 1]);
                    textView.setTypeface(null, Typeface.BOLD);
                } else if (j == 0) {
                    textView.setText(row.get(i - 1));
                    textView.setTypeface(null, Typeface.BOLD);
                } else if (i != 0 && j != 0)
                    textView.setText(Integer.toString(manager.getPlayers()[j - 1].scoreHistory.get(i - 1)));

                textView.setGravity(Gravity.CENTER);
                textView.setPadding(30, 5, 5, 5);
                textView.setTextSize(15);
                tableRow.addView(textView);
            }
            tableLayout.addView(tableRow);
        }

        ScrollView sv = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.MATCH_PARENT);
        sv.setPadding(10, 20, 10, 20);
        sv.setLayoutParams(scrollParams);
        sv.setSmoothScrollingEnabled(true);
        sv.addView(tableLayout);

        int currentAPILevel = android.os.Build.VERSION.SDK_INT;
        if(currentAPILevel > android.os.Build.VERSION_CODES.KITKAT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.BidCustom));
            builder.setView(sv);
            builder.setCancelable(false);
            builder.setTitle("Scoreboard");

            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    reset();
                }
            });
            builder.show();
        } else {
            android.support.v7.app.AlertDialog.Builder builder =
                    new android.support.v7.app.AlertDialog.Builder(this, R.style.BidCustom);
            builder.setView(sv);
            builder.setCancelable(false);
            builder.setTitle("Scoreboard");

            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    reset();
                }
            });
            builder.show();
        }
    }

    //Call when the end piles and the scores displayed on top of the piles need be redisplayed.
    public void displayEndPiles(List<Integer> scores) {
        TextView[] scoreViews = new TextView[]{(TextView) findViewById(R.id.bottomScoreView), (TextView) findViewById(R.id.leftScoreView),
                (TextView) findViewById(R.id.topScoreView), (TextView) findViewById(R.id.rightScoreView)};

        for (int i = 0; i < 4; i++) {
            String obtained = Integer.toString(((BridgePlayer) manager.getPlayers()[i]).obtained);
            String bidded = Integer.toString(((BridgePlayer) manager.getPlayers()[i]).guess);
            scoreViews[i].setText(obtained + "/" + bidded);
        }
    }

    public void displayWaitScreenBid(final int currentPlayer) {
        final Dialog alertDialog = new Dialog(this);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.wait_screen);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView text = (TextView) alertDialog.findViewById(R.id.wait_text);
        text.setText("Player " + (currentPlayer + 1) + ": Click anywhere to continue.");
        text.setTextColor(Color.WHITE);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                displayHands(currentPlayer, true, false);
                openGuessDialog(currentPlayer);
            }
        });
        alertDialog.show();
    }

    //Opens the guess dialog - fit for German Bridge for now.
    public void openGuessDialog(final int currentPlayer) {
        displayHands(currentPlayer, false, false);

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        guess = -1;

        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.HORIZONTAL);

        TextView text = new TextView(this);
        text.setText("Player " + (currentPlayer + 1) + " bid");
        text.setTextColor(getResources().getColor(R.color.spades_blue));
        text.setTextSize(22);
        text.setPadding(30, 50, 30, 30);
        text.setTypeface(null, Typeface.BOLD);
        Button[] buttons = new Button[manager.potsFinished + 1];

        for (int i = 0; i <= manager.potsFinished; i++) {
            final RadioButton button = new RadioButton(this);
            button.setText(Integer.toString(i));
            button.setTextSize(20);
            if (i != manager.getPlayers()[currentPlayer].hand.size() - ((BridgeManager)manager).addedGuesses) {
                button.setTextColor(getResources().getColor(R.color.spades_blue));
                button.setBackgroundResource(R.drawable.bid_selected);
            } else {
                button.setTextColor(Color.RED);
                button.setClickable(false);
            }
            button.setButtonDrawable(R.color.transparent);
            button.setPadding(50, 0, 50, 0);
            button.setTag(i);
            buttons[i] = button;
            group.addView(button);
        }
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llp.setMargins(50, 0, 0, 0);
        layout.addView(group, llp);
        horizontalScrollView.addView(layout);
        horizontalScrollView.setLayoutParams(new AbsListView.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Dialog d = null;
        int currentAPILevel = android.os.Build.VERSION.SDK_INT;
        if(currentAPILevel > android.os.Build.VERSION_CODES.KITKAT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.BidCustom));
            builder.setCancelable(false);
            builder.setCustomTitle(text);
            builder.setView(horizontalScrollView);
            d = builder.create();
        } else {
            android.support.v7.app.AlertDialog.Builder builder =
                    new android.support.v7.app.AlertDialog.Builder(this, R.style.BidCustom);
            builder.setCancelable(false);
            builder.setCustomTitle(text);
            builder.setView(horizontalScrollView);
            d = builder.create();
        }
        final Dialog finalDialog = d;

        for (final Button button : buttons) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SoundManager.playButtonClickSound();
                    if ((guess != manager.getPlayers()[currentPlayer].hand.size() - ((BridgeManager)manager).addedGuesses) &&
                            (guess == (Integer) button.getTag())) {
                        ((BridgeManager) manager).addedGuesses += guess;
                        ((BridgePlayer) manager.players[currentPlayer]).guess = guess;
                        displayEndPiles(scores);

                        finalDialog.dismiss();
                        guessCount++;
                        //Finished guessing, now move to actual gameplay.
                        if (guessCount == 4) {
                            if (manager.pot.size() == 0)
                                currentPlayerInteracting = manager.findStartPlayer();
                            if (isBot[currentPlayerInteracting])
                                botHandle(250);
                            else {
                                if (botCount != 3) {
                                    displayHands(-1, false, true);
                                    displayWaitScreen(currentPlayerInteracting);
                                } else
                                    displayHands(currentPlayerInteracting, true, true);
                                canClick = true;
                            }
                            return;
                        }

                        //Otherwise, we prompt the guess dialog for the next player.
                        int player = (currentPlayer + 1) % 4;
                        while (isBot[player]) {
                            if (guessCount == 4) {
                                if (manager.pot.size() == 0)
                                    currentPlayerInteracting = manager.findStartPlayer();
                                if (isBot[currentPlayerInteracting])
                                    botHandle(250);
                                else {
                                    if (botCount != 3) {
                                        displayHands(-1, false, true);
                                        displayWaitScreen(currentPlayerInteracting);
                                    } else
                                        displayHands(currentPlayerInteracting, true, true);
                                    canClick = true;
                                }
                            }

                            guessCount++;
                            ((BridgePlayer) manager.getPlayers()[player]).guess = BridgeAI.getBid(player, (BridgeManager) manager);
                            displayEndPiles(scores);
                            ((BridgeManager) manager).addedGuesses += ((BridgePlayer) manager.getPlayers()[player]).guess;
                            if (guessCount == 4) {
                                if (manager.pot.size() == 0)
                                    currentPlayerInteracting = manager.findStartPlayer();
                                if (isBot[currentPlayerInteracting])
                                    botHandle(250);
                                else {
                                    if (botCount != 3) {
                                        displayHands(-1, false, true);
                                        displayWaitScreen(currentPlayerInteracting);
                                    } else
                                        displayHands(currentPlayerInteracting, true, true);
                                    canClick = true;
                                }
                                return;
                            }
                            player = (player + 1) % 4;
                        }

                        if (guessCount < 4) {
                            if (botCount != 3) {
                                displayHands(-1, false, true);
                                displayWaitScreenBid(player);
                            } else
                                openGuessDialog(player);
                        }
                    } else {
                        guess = (Integer) button.getTag();
                    }
                }
            });
        }

        //d.getWindow().setLayout(findViewById(R.id.bid).getWidth(), findViewById(R.id.bid).getHeight());
        d.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.copyFrom(d.getWindow().getAttributes());
        lp.width = biddingWidthPX;
        lp.height = biddingHeightPX;
        d.getWindow().setAttributes(lp);
    }

    //Call when the hands have been updated and need be redisplayed.
    //We add the cardsClickable flag for when the player waits for the AI to finish turns.
    public void displayHands(int player, boolean cardsClickable, boolean cardsShadeable) {
        //Remove all old cards first
        cardViews.clear();
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
                    if (!(manager.cardSelectable(selectCard, finishedSwapping, i)) && cardsShadeable) {
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
                cardButton.setScaleType(ImageView.ScaleType.FIT_CENTER);

                switch (i) {
                    case 0:
                        restParams.setMargins(deltaX, bottomTopMarginPX - deltaY, 0, 0);
                        cardButton.setRotation(theta);
                        bottom.addView(cardButton, restParams);
                        break;
                    case 1:
                        restParams.setMargins(deltaY, deltaX, 0, 0);
                        cardButton.setRotation(90 + theta);
                        left.addView(cardButton, restParams);
                        break;
                    case 2:
                        restParams.setMargins(deltaX, deltaY, 0, 0);
                        cardButton.setRotation(180 - theta);
                        top.addView(cardButton, restParams);
                        break;
                    case 3:
                        restParams.setMargins(rightLeftMarginPX - deltaY, deltaX, 0, 0);
                        cardButton.setRotation(270 - theta);
                        right.addView(cardButton, restParams);
                        break;
                }
                cardButton.setId(temporaryID++);
                cardViews.add(cardButton);
                //Set the deltaX/theta parameters for the next card/loop iteration.
                //Consequence of more space horizontally than vertically; set smaller distance between cards vertically.
                deltaX += cardDeltaXPX;
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

    public void dealCards() {
        long currentTimeDelay = 0;
        final int[] initialCoordinates = new int[2];
        findViewById(R.id.anchor).getLocationOnScreen(initialCoordinates);
        for (int j = 0; j < manager.players[0].hand.size(); j++) {
            final int cardsDisplayed = j;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SoundManager.playDealCardSound();
                    GameAnimation.dealSingleCards(BridgeActivity.this, new Runnable() {
                        @Override
                        public void run() {
                            displayIntermediateHands(cardsDisplayed);

                            if (cardsDisplayed == manager.players[0].hand.size() - 1) {
                                //If the game is loaded, then calling findStartPlayer incorrectly mutates the st. player mid-round.
                                int player = isGameLoaded ? manager.startPlayer : manager.findStartPlayer();
                                while (isBot[player]) {
                                    if (guessCount == 4) {
                                        if (manager.pot.size() == 0)
                                            currentPlayerInteracting = manager.findStartPlayer();
                                        if (isBot[currentPlayerInteracting])
                                            botHandle(250);
                                        else {
                                            if (botCount != 3) {
                                                displayHands(-1, false, true);
                                                displayWaitScreen(currentPlayerInteracting);
                                            } else
                                                displayHands(currentPlayerInteracting, true, true);
                                            canClick = true;
                                        }
                                        return;
                                    }
                                    ((BridgePlayer) manager.getPlayers()[player]).guess = BridgeAI.getBid(player, (BridgeManager) manager);
                                    displayEndPiles(scores);
                                    ((BridgeManager) manager).addedGuesses += ((BridgePlayer) manager.getPlayers()[player]).guess;
                                    guessCount++;
                                    player = (player + 1) % 4;
                                }
                                if (guessCount == 4) {
                                    if (manager.pot.size() == 0)
                                        currentPlayerInteracting = isGameLoaded ? manager.startPlayer : manager.findStartPlayer();
                                    if (isBot[currentPlayerInteracting])
                                        botHandle(250);
                                    else {
                                        if (botCount != 3) {
                                            displayHands(-1, false, true);
                                            displayWaitScreen(currentPlayerInteracting);
                                        } else
                                            displayHands(currentPlayerInteracting, true, true);
                                        canClick = true;
                                    }
                                    return;
                                }
                                if (botCount != 3) {
                                    displayHands(-1, false, true);
                                    displayWaitScreenBid(player);
                                }
                                else
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
            FileInputStream fis = this.openFileInput("save_bridge");
            ObjectInputStream is = new ObjectInputStream(fis);
            super.loadGame(is);
            this.manager = (BridgeManager) is.readObject();
            this.botCount = is.readInt();
            this.guessCount = is.readInt();
            this.guess = is.readInt();
            is.close();
            fis.close();
            deleteFile("save_bridge");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveGame() {
        String filename = "save_bridge";
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