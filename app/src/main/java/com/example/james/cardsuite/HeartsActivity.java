package com.example.james.cardsuite;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeartsActivity extends GameActivity implements Serializable {
    private boolean finishedSwapping = false, canClick = true, displayedHeartsBroken = false;
    private List<List<Card>> chosenLists = new ArrayList<List<Card>>();
    private List<Card> chosenCards = new ArrayList<Card>();
    private Map<View, Integer> animationsActive = new HashMap<View, Integer>();
    public int heartsBrokenPX, botCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearts);

        Intent intent = getIntent();
        boolean loadGame = intent.getBooleanExtra("loadGame", false);
        if (loadGame) {
            this.loadGame();
            this.displayPot();
        } else {
            this.isBot = intent.getBooleanArrayExtra("isBot");
            manager = new HeartsManager(isBot);
            currentPlayerInteracting = 0;
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
        }

        displayEndPiles(scores);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        heartsBrokenPX = (int)Math.ceil(150 * metrics.density);

        View[] potViews = new View[] {findViewById(R.id.leftPotCard),findViewById(R.id.topPotCard),
                findViewById(R.id.rightPotCard),findViewById(R.id.bottomPotCard)};
        for(View card : potViews) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)card.getLayoutParams();
            params.width = cardWidthPX;
            params.height = cardHeightPX;
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
        saveGame();
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    public void findStartPlayer() {
        // finds the first player
        if (!foundStartPlayer) {
            currentPlayerInteracting = manager.findStartPlayer();
            foundStartPlayer = true;
        }
    }

    public void playerHandle(View v) {
        //Play sounds only if we're done swapping in hearts or are in any other game mode.
        if (finishedLoading && finishedSwapping) {
            int chosenSound = r.nextInt(3);
            soundPools[chosenSound].play(sounds[chosenSound], sfxVolume, sfxVolume, 0, 0, 1);
        }

        if (!(manager.isGameOver())) {
            // Handle player choosing cards
            int chosen = getCardIndex(v);
            Card chosenCard = manager.getPlayers()[currentPlayerInteracting].hand.get(chosen);

            if (!finishedSwapping) {
                // Handle the player swapping cards
                int swapRound = (manager.getPotsFinished() - 1) % 4;
                if (swapRound != 3) {
                    this.chooseCards(chosenCard, swapRound, v);
                } else {
                    finishedSwapping = true;
                    canClick = true;
                }
            } else {
                // handle cards being tossed in the pot until all cards are gone (13 turns).
                manager.potHandle(chosen, currentPlayerInteracting);
                if(!displayedHeartsBroken && ((HeartsManager)manager).heartsBroken) {
                    GameAnimation.showHeartsBroken(this);
                    displayedHeartsBroken = true;
                }

                final int lastPlayer = manager.startPlayer == 0 ? 3 : manager.startPlayer-1;
                final boolean isRoundOver = manager.players[lastPlayer].hand.size() == 0;
                final int currentPotSize = manager.pot.size();
                GameAnimation.placeCard(HeartsActivity.this, v, new Runnable() {
                    @Override
                    public void run() {
                        displayHands(currentPlayerInteracting, false);

                        potClear();
                        displayPot();

                        if (manager.pot.size() > 0)
                            restOfRoundHandle();

                        Log.i("Continue?", Boolean.toString((currentPotSize != 4 && !isRoundOver)));
                        if(currentPotSize != 4 && !isRoundOver) {
                            if (manager.getPlayers()[currentPlayerInteracting].isBot)
                                botHandle(250);
                            else {
                                if (botCount != 3) {
                                    displayHands(-1, false);
                                    displayWaitScreen(currentPlayerInteracting);
                                } else
                                    displayHands(currentPlayerInteracting, true);
                                canClick = true;
                            }
                        }
                    }
                }, currentPlayerInteracting);
            }

            //We copy the code block above because if we're done swapping, we want to continue execution ONLY when the animation is done.
            if (!finishedSwapping && manager.getPlayers()[currentPlayerInteracting].isBot) {
                botHandle(250);
            } else if (!finishedSwapping && chosenCards.size() == 0) {
                //This is only executed to display the hand of the next player during the swapping phase/end of turn in game phase.
                if (botCount != 3) {
                    displayHands(-1, false);
                    displayWaitScreen(currentPlayerInteracting);
                }
                else
                    displayHands(currentPlayerInteracting, true);

                canClick = true;
            }
        } else
            endGame();
    }

    public void botHandle(final long delay) {
        if (!manager.isGameOver()) {
            if (!finishedSwapping) {
                final int swapRound = (manager.getPotsFinished() - 1) % 4;
                System.out.println(swapRound);
                if (swapRound != 3) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            List<Card> botChosen = ((HeartsAI) manager.getPlayers()[currentPlayerInteracting]).chooseSwap();

                            for (int i = 0; i < botChosen.size(); i++) {
                                View chosenView = findViewByCard(botChosen.get(i));
                                GameAnimation.selectSwappedCard(chosenView, currentPlayerInteracting);
                                animationsActive.put(chosenView, currentPlayerInteracting);
                            }

                            chosenLists.add(botChosen);
                            currentPlayerInteracting++;

                            if (currentPlayerInteracting == 4)
                                swapCards(swapRound);

                            if (finishedSwapping && (manager.pot.size() > 0))
                                restOfRoundHandle();

                            if (manager.getPlayers()[currentPlayerInteracting].isBot)
                                botHandle(250);
                            else {
                                displayHands(-1, false);
                                if (botCount != 3) {
                                    displayHands(-1, false);
                                    displayWaitScreen(currentPlayerInteracting);
                                }
                                else
                                    displayHands(currentPlayerInteracting, true);
                                canClick = true;
                            }
                        }
                    }, delay);
                } else
                    finishedSwapping = true;
            } else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Card botMove = ((HeartsAI) manager.getPlayers()[currentPlayerInteracting]).makeMove(currentPlayerInteracting, manager.startPlayer, (HeartsManager) manager);

                        ((HeartsManager) manager).potHandle(botMove, currentPlayerInteracting);
                        if(!displayedHeartsBroken && ((HeartsManager)manager).heartsBroken) {
                            GameAnimation.showHeartsBroken(HeartsActivity.this);
                            displayedHeartsBroken = true;
                        }

                        final int lastPlayer = manager.startPlayer == 0 ? 3 : manager.startPlayer-1;
                        final boolean isRoundOver = manager.players[lastPlayer].hand.size() == 0;
                        final int currentPotSize = manager.pot.size();

                        ImageView cardView = (ImageView) findViewByCard(botMove);
                        GameAnimation.placeCard(HeartsActivity.this, cardView, new Runnable() {
                            @Override
                            public void run() {
                                potClear();
                                displayPot();

                                displayHands(lastNonBot, false);
                                int chosenSound = r.nextInt(3);
                                soundPools[chosenSound].play(sounds[chosenSound], sfxVolume, sfxVolume, 0, 0, 1);

                                if (finishedSwapping && (manager.pot.size() > 0))
                                    restOfRoundHandle();

                                if(currentPotSize != 4 && !isRoundOver) {
                                    if (manager.getPlayers()[currentPlayerInteracting].isBot)
                                        botHandle(250);
                                    else {
                                        if (botCount != 3) {
                                            displayHands(-1, false);
                                            displayWaitScreen(currentPlayerInteracting);
                                        } else
                                            displayHands(currentPlayerInteracting, true);
                                        canClick = true;
                                    }
                                }
                            }
                        }, currentPlayerInteracting);

                    }
                }, delay);
            }
        } else {
            endGame();
        }
    }

    public void restOfRoundHandle() {
        currentPlayerInteracting = (currentPlayerInteracting + 1) % 4;

        if (manager.pot.size() == 4)
            endPot();
    }

    public void endGame() {
        // The game is done - pass all relevant information for results activity to display.
        // Passing manager just in case for future statistics if needbe.
        Intent intent = new Intent(HeartsActivity.this, ResultsActivity.class);
        intent.putExtra("manager", manager);
        intent.putExtra("players", manager.getPlayers());
        startActivity(intent);
        finish();
    }

    public void gameClick(View v) {
        //Prevents spam-clicking before the last button click is done.
        if (!canClick || SystemClock.elapsedRealtime() - lastClickTime < 200)
            return;
        canClick = false;
        lastClickTime = SystemClock.elapsedRealtime();

        super.gameClick(v);
        this.playerHandle(v);
    }

    public void endPot() {
        currentPotTurn++;
        manager.potAnalyze(); //sets the new start player for the next pot

        currentPlayerInteracting = manager.startPlayer;
        HeartsPlayer winPlayer = (HeartsPlayer) manager.getPlayers()[manager.startPlayer];
        winPlayer.obtainedCards = true;
        for (Card c : manager.pot.values())
            winPlayer.endPile.add(c);

        manager.pot.clear();

        roundScores.clear();
        for (int i = 0; i < manager.getPlayers().length; i++)
            roundScores.add(((HeartsPlayer) manager.getPlayers()[i]).tallyRoundScore());

        GameAnimation.collectEndPile(HeartsActivity.this, new Runnable() {
            @Override
            public void run() {
                displayEndPiles(roundScores);
                manager.usedCards.addAll(manager.pot.values());
                potClear();
                displayPot();

                if (currentPotTurn == 13)
                    finishedRound();
                else {
                    if (manager.getPlayers()[currentPlayerInteracting].isBot)
                        botHandle(250);
                    else {
                        if (botCount != 3) {
                            displayHands(-1, false);
                            displayWaitScreen(currentPlayerInteracting);
                        } else
                            displayHands(currentPlayerInteracting, true);
                        canClick = true;
                    }
                }
            }
        }, currentPlayerInteracting);
    }

    public void finishedRound() {
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
            roundScores.add(((HeartsPlayer) manager.getPlayers()[i]).tallyRoundScore());
        displayEndPiles(roundScores);
        reset();
        displayScoreTable(new Runnable() {
            @Override
            public void run() {
                dealCards();
            }
        });
    }

    public void chooseCards(Card chosenCard, int swapRound, View v) {
        if (!chosenCard.isClicked) {
            GameAnimation.selectSwappedCard(v, currentPlayerInteracting);
            animationsActive.put(v, currentPlayerInteracting);
            chosenCard.isClicked = true;
        } else {
            GameAnimation.unselectSwappedCard(v, currentPlayerInteracting);
            animationsActive.remove(v);
            chosenCard.isClicked = false;
        }

        if (!finishedSwapping) {
            //Play swapping sound.
            soundPools[3].play(sounds[3], sfxVolume, sfxVolume, 0, 0, 1);

            // Swap the cards between players.
            if (currentPlayerInteracting != 4) {
                //If the chosen card is already chosen, deselect it - otherwise, add it to our chosen cards.
                if (chosenCards.contains(chosenCard))
                    chosenCards.remove(chosenCard);
                else {
                    chosenCards.add(chosenCard);
                }

                if (chosenCards.size() < 3) {
                    canClick = true;
                    return;
                }

                List<Card> tempCards = new ArrayList<>();
                tempCards.addAll(chosenCards);
                chosenCards.clear();
                chosenLists.add(tempCards);
            }
            currentPlayerInteracting++;

            if (currentPlayerInteracting == 4)
                swapCards(swapRound);
        }
    }

    public void swapCards(int swapRound) {
        for (int i = 0; i < 4; i++) {
            manager.swapCards(chosenLists.get(i), i, swapRound);
            manager.getPlayers()[i].organize();
        }

        soundPools[5].play(sounds[5], sfxVolume, sfxVolume, 0, 1, 1);
        GameAnimation.swapCards(this, swapRound, new Runnable() {
            @Override
            public void run() {
                soundPools[5].stop(sounds[5]);

                //This is run under the assumption the start player will be found below as the animation is run.
                findStartPlayer();
                if (botCount != 3) {
                    displayHands(-1, false);
                    displayWaitScreen(findNextNonBot(manager.startPlayer));
                }
                else
                    displayHands(findNextNonBot(manager.startPlayer), true);
                canClick = true;
            }
        }, animationsActive);

        findStartPlayer();
        finishedSwapping = true;
    }

    public int findNextNonBot(int currentPlayer) {
        for (int i = 0; i < 4; i++) {
            if (!(manager.getPlayers()[currentPlayer].isBot))
                return currentPlayer;

            currentPlayer = (currentPlayer + 1) % 4;
        }
        return currentPlayer;
    }

    // reshuffles deck, increments round count, resets all variables for the next round.
    public void reset() {
        manager.reset();
        int swapRound = (manager.getPotsFinished() - 1) % 4;
        if (swapRound != 4)
            finishedSwapping = false;
        displayedHeartsBroken = false;
        animationsActive = new HashMap<View, Integer>();
        initialOutputWritten = false;
        buttonsPresent = false;
        foundStartPlayer = false;
        currentPotTurn = 0;
        currentPlayerInteracting = 0;
        chosenLists.clear();
        chosenCards.clear();
    }

    //Call when the end piles and the scores displayed on top of the piles need be redisplayed.
    public void displayEndPiles(List<Integer> scores) {
        TextView[] scoreViews = new TextView[]{(TextView) findViewById(R.id.bottomScoreView), (TextView) findViewById(R.id.leftScoreView),
                (TextView) findViewById(R.id.topScoreView), (TextView) findViewById(R.id.rightScoreView)};

        for (int i = 0; i < 4; i++) {
            scoreViews[i].setText(Integer.toString(scores.get(i)));
        }
    }

    //Call when the hands have been updated and need be redisplayed.
    public void displayHands(int player, boolean cardsClickable) {
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
                cardButton.setMaxHeight(cardHeightPX);
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

    public void dealCards() {
        long currentTimeDelay = 0;
        final int[] initialCoordinates = new int[2];
        findViewById(R.id.anchor).getLocationOnScreen(initialCoordinates);
        for (int j = 0; j < manager.players[0].hand.size(); j++) {
            final int cardsDisplayed = j;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    soundPools[4].play(sounds[4], sfxVolume, sfxVolume, 0, 0, 1);
                    GameAnimation.dealSingleCards(HeartsActivity.this, new Runnable() {
                        @Override
                        public void run() {
                            displayIntermediateHands(cardsDisplayed);

                            if (cardsDisplayed == manager.players[0].hand.size() - 1) {
                                if (manager.getPlayers()[currentPlayerInteracting].isBot)
                                    botHandle(250);
                                else {
                                    if (botCount != 3) {
                                        displayHands(-1, false);
                                        displayWaitScreen(currentPlayerInteracting);
                                    }
                                    else {
                                        displayHands(currentPlayerInteracting, true);
                                        canClick = true;
                                    }
                                }
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
            FileInputStream fis = this.openFileInput("save_hearts");
            ObjectInputStream is = new ObjectInputStream(fis);
            super.loadGame(is);
            this.manager = (HeartsManager) is.readObject();
            this.finishedSwapping = is.readBoolean();
            this.displayedHeartsBroken = is.readBoolean();
            int size = is.readInt();
            botCount = is.readInt();
            for (int i = 0; i < size; i++)
                this.chosenLists.add((List<Card>) is.readObject());
            is.close();
            fis.close();
            deleteFile("save_hearts");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void saveGame() {
        String filename = "save_hearts";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
            super.saveGame(objectStream);
            objectStream.writeObject(this.manager);
            objectStream.writeBoolean(finishedSwapping);
            objectStream.writeBoolean(displayedHeartsBroken);
            objectStream.writeInt(chosenLists.size());
            objectStream.writeInt(botCount);
            for (int i = 0; i < chosenLists.size(); i++)
                objectStream.writeObject(chosenLists.get(i));
            for (Card c : manager.getPlayers()[currentPlayerInteracting].hand)
                if (c.isClicked)
                    c.isClicked = false;
            objectStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Opens the guess dialog - inapplicable for hearts however, refactoring can be done to differentiate hearts from other games.
    public void openGuessDialog(final int currentPlayer) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hearts, menu);
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
