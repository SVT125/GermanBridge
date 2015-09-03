package com.example.james.cardsuite;

import android.animation.AnimatorSet;
import android.content.Context;
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
    private boolean finishedSwapping = false;
    private List<List<Card>> chosenLists = new ArrayList<List<Card>>();
    private List<Card> chosenCards = new ArrayList<Card>();
    Map<Pair<Integer,View>,AnimatorSet> animationsActive = new HashMap<Pair<Integer,View>,AnimatorSet>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearts);

        Intent intent = getIntent();
        boolean loadGame = intent.getBooleanExtra("loadGame", false);
        if (loadGame) {
            this.loadGame();
            this.displayPot();
        }
        else {
            this.isBot = intent.getBooleanArrayExtra("isBot");
            manager = new HeartsManager(isBot);
            currentPlayerInteracting = 0;
        }

        displayEndPiles(scores);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dealCards();
            }
        }, 500);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (manager.getPlayers()[currentPlayerInteracting].isBot)
                    botHandle(250);
                else
                    displayHands(0, true);
            }
        }, 3000);
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
        if ((finishedLoading && finishedSwapping)) {
            int chosenSound = r.nextInt(3);
            soundPools[chosenSound].play(sounds[chosenSound], 1, 1, 0, 0, 1);
        }

        if (!(manager.isGameOver())) {
            // Handle player choosing cards
            int chosen = getCardIndex(v);
            Card chosenCard = manager.getPlayers()[currentPlayerInteracting].hand.get(chosen);

            if (!finishedSwapping) {
                // Handle the player swapping cards
                int swapRound = manager.getPotsFinished() % 4;
                if (swapRound != 3)
                    this.chooseCards(chosenCard, swapRound, v);
                else
                    finishedSwapping = true;
            } else {
                // handle cards being tossed in the pot until all cards are gone (13 turns).
                manager.potHandle(chosen, currentPlayerInteracting);
                GameAnimation.placeCard(HeartsActivity.this, v, currentPlayerInteracting);
                displayHands(currentPlayerInteracting, false);

                potClear();
                displayPot();
            }

            if (finishedSwapping && (manager.pot.size() > 0))
                restOfRoundHandle();

            if (manager.getPlayers()[currentPlayerInteracting].isBot) {
                botHandle(250);
            } else if(chosenCards.size() == 3){
                displayHands(currentPlayerInteracting,true);
                if (!(chosenLists.size() == 4))
                    chosenCards.clear();
            }

            return;
        }
        else {
            endGame();
        }
    }

    public void restOfRoundHandle() {
        if (currentPlayerInteracting == manager.startPlayer)
            potClear();

        displayPot();

        currentPlayerInteracting = (currentPlayerInteracting + 1) % 4;

        if (manager.pot.size() == 4)
            endPot();
    }

    public void botHandle(final long delay) {
        if (!manager.isGameOver()) {
            if (!finishedSwapping) {
                final int swapRound = manager.getPotsFinished() % 4;
                if (swapRound != 3) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            List<Card> botChosen = ((HeartsAI) manager.getPlayers()[currentPlayerInteracting]).chooseSwap();

                            for(int i = 0; i < botChosen.size(); i++)
                                animationsActive.put(new Pair<Integer, View>(currentPlayerInteracting,findViewByCard(botChosen.get(i))),
                                        GameAnimation.selectSwappedCard(HeartsActivity.this,
                                                findViewByCard(botChosen.get(i)),currentPlayerInteracting));

                            chosenLists.add(botChosen);
                            currentPlayerInteracting++;

                            if (currentPlayerInteracting == 4)
                                swapCards(swapRound);

                            if (finishedSwapping && (manager.pot.size() > 0))
                                restOfRoundHandle();

                            if (manager.getPlayers()[currentPlayerInteracting].isBot)
                                botHandle(250);
                            else
                                displayHands(currentPlayerInteracting, true);
                        }
                    }, delay);
                } else
                    finishedSwapping = true;
            } else {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Card botMove = ((HeartsAI) manager.getPlayers()[currentPlayerInteracting]).makeMove(currentPlayerInteracting, manager.startPlayer, (HeartsManager) manager);

                        ((HeartsManager) manager).potHandle(botMove, currentPlayerInteracting);

                        ImageView cardView = (ImageView)findViewByCard(botMove);
                        GameAnimation.placeCard(HeartsActivity.this, cardView, currentPlayerInteracting);
                        potClear();
                        displayPot();

                        int chosenSound = r.nextInt(3);
                        soundPools[chosenSound].play(sounds[chosenSound], 1, 1, 0, 0, 1);

                        if (finishedSwapping && (manager.pot.size() > 0))
                            restOfRoundHandle();

                            if (manager.getPlayers()[currentPlayerInteracting].isBot)
                                botHandle(250);
                            else
                                displayHands(currentPlayerInteracting, true);
                    }
                }, delay);
            }
        } else {
            endGame();
        }
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
        if ((!finishedSwapping && SystemClock.elapsedRealtime() - lastClickTime < 1000) ||
                (finishedSwapping && SystemClock.elapsedRealtime() - lastClickTime < 1750)){
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        super.gameClick(v);
        this.playerHandle(v);
    }

    public void endPot() {
        Handler handler = new Handler();

        currentPotTurn++;
        manager.potAnalyze(); //sets the new start player for the next pot
        currentPlayerInteracting = manager.startPlayer;
        for (Card c : manager.pot.values())
            ((HeartsPlayer) manager.getPlayers()[manager.startPlayer]).endPile.add(c);

        roundScores.clear();
        for (int i = 0; i < manager.getPlayers().length; i++)
            roundScores.add(((HeartsPlayer) manager.getPlayers()[i]).tallyRoundScore());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GameAnimation.collectEndPile(HeartsActivity.this, new Runnable() {
                    @Override
                    public void run() {
                        displayEndPiles(roundScores);
                        manager.usedCards.addAll(manager.pot.values());
                        manager.pot.clear();

                        if (currentPotTurn == 13)
                            finishedRound();
                    }
                },currentPlayerInteracting);
            }
        },75);
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
        displayScoreTable();
    }

    public void chooseCards(Card chosenCard, int swapRound, View v) {
        if (!chosenCard.isClicked) {
            chosenCard.isClicked = true;
            v.setBackgroundResource(R.drawable.card_border);
            animationsActive.put(new Pair<Integer, View>(currentPlayerInteracting,findViewByCard(chosenCard)), GameAnimation.
                    selectSwappedCard(this, v, currentPlayerInteracting));
        } else {
            v.setBackgroundResource(0);
            chosenCard.isClicked = false;
            animationsActive.remove(new Pair<Integer,View>(currentPlayerInteracting,v));
        }

        if (!finishedSwapping) {
            //Play swapping sound.
            soundPools[3].play(sounds[3], 1, 1, 0, 0, 1);

            // Swap the cards between players.
            if (currentPlayerInteracting != 4) {
                //If the chosen card is already chosen, deselect it - otherwise, add it to our chosen cards.
                if (chosenCards.contains(chosenCard))
                    chosenCards.remove(chosenCard);
                else {
                    chosenCards.add(chosenCard);
                }

                if (chosenCards.size() < 3)
                    return;

                List<Card> tempCards = new ArrayList<>();
                tempCards.addAll(chosenCards);
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

        GameAnimation.swapCards(this,swapRound,animationsActive);

        finishedSwapping = true;
        findStartPlayer();
    }

    // reshuffles deck, increments round count, resets all variables for the next round.
    public void reset() {
        manager.reset();
        finishedSwapping = false;
        animationsActive = new HashMap<Pair<Integer,View>,AnimatorSet>();
        initialOutputWritten = false;
        buttonsPresent = false;
        foundStartPlayer = false;
        currentPotTurn = 0;
        currentPlayerInteracting = 0;
        chosenLists.clear();
        chosenCards.clear();
        dealCards();
    }

    //Call when the end piles and the scores displayed on top of the piles need be redisplayed.
    public void displayEndPiles(List<Integer> scores) {
        TextView[] scoreViews = new TextView[]{(TextView) findViewById(R.id.bottomScore), (TextView) findViewById(R.id.leftScore),
                (TextView) findViewById(R.id.topScore), (TextView) findViewById(R.id.rightScore)};
        ImageView[] pileViews = new ImageView[]{(ImageView) findViewById(R.id.bottomPile), (ImageView) findViewById(R.id.leftPile),
                (ImageView) findViewById(R.id.topPile), (ImageView) findViewById(R.id.rightPile)};

        for (int i = 0; i < 4; i++) {
            //Update the score, but remove or update the pile if it exists.
            pileViews[i].setMaxHeight(115);
            pileViews[i].setAdjustViewBounds(true);
            if (scores.get(i) != 0) {
                pileViews[i].setImageResource(getResources().getIdentifier("cardback", "drawable", getPackageName()));
                scoreViews[i].setVisibility(View.VISIBLE);
                scoreViews[i].setText(Integer.toString(scores.get(i)));
            } else {
                pileViews[i].setImageResource(0);
                scoreViews[i].setVisibility(View.INVISIBLE);
            }
        }
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
            float initialTheta = (float) -4.6 * manager.getPlayers()[i].hand.size() / 2;
            for (int j = 0; j < manager.getPlayers()[i].hand.size(); j++) {
                float theta = (float) (initialTheta + 4.6 * j);
                deltaY = (int) (2.5 * (30 - Math.pow(j - manager.getPlayers()[i].hand.size() / 2, 2))); //Truncate the result of the offset

                if(manager.getPlayers()[i].hand.size() % 2 != 0 && j == (manager.getPlayers()[i].hand.size()-1)/2)
                    theta = 0;

                RelativeLayout.LayoutParams restParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                //How to treat and initialize the other cards depending on whether the current player or any other.
                ImageView cardButton;
                if (i == player) {
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
                    if (!(manager.cardSelectable(selectCard, finishedSwapping, i))) {
                        cardButton.setColorFilter(Color.parseColor("#78505050"), PorterDuff.Mode.SRC_ATOP);
                        cardButton.setClickable(false);
                    }
                } else {
                    cardButton = new ImageView(this);
                    cardButton.setImageResource(getResources().getIdentifier("cardback", "drawable", getPackageName()));
                }

                cardButton.setTag(manager.getPlayers()[i].hand.get(j));
                cardButton.setPadding(3, 3, 3, 3);
                if(!cardsClickable)
                    cardButton.setClickable(false);
                cardButton.setMaxHeight(115);
                cardButton.setAdjustViewBounds(true);

                switch (i) {
                    case 0:
                        restParams.setMargins(deltaX, 95 - deltaY, 0, 0);
                        cardButton.setRotation(theta);
                        bottom.addView(cardButton, restParams);
                        break;
                    case 1:
                        restParams.setMargins(100 + deltaY, deltaX, 0, 0);
                        cardButton.setRotation(90 + theta);
                        left.addView(cardButton, restParams);
                        break;
                    case 2:
                        restParams.setMargins(deltaX, 60 + deltaY, 0, 0);
                        cardButton.setRotation(180 - theta);
                        top.addView(cardButton, restParams);
                        break;
                    case 3:
                        restParams.setMargins(115 - deltaY, deltaX, 0, 0);
                        cardButton.setRotation(90 - theta);
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
        Handler handler = new Handler();
        long currentTimeDelay = 0;
        final int[] initialCoordinates = new int[2];
        findViewById(R.id.anchor).getLocationOnScreen(initialCoordinates);

        for(int j = 0; j < manager.players[0].hand.size(); j++) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    GameAnimation.dealSingleCards(HeartsActivity.this, initialCoordinates);
                }
            }, currentTimeDelay);

            currentTimeDelay += 100;
            final int cardsDisplayed = j;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayIntermediateHands(cardsDisplayed);
                }
            },currentTimeDelay);
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                displayHands(currentPlayerInteracting, true);
            }
        },currentTimeDelay+100);
    }

    public void loadGame() {

        try {
            FileInputStream fis = this.openFileInput("save_hearts");
            ObjectInputStream is = new ObjectInputStream(fis);
            this.manager = (HeartsManager) is.readObject();
            this.currentPlayerInteracting = is.readInt();
            this.currentPotTurn = is.readInt();
            this.foundStartPlayer = is.readBoolean();
            this.finishedSwapping = is.readBoolean();
            this.buttonsPresent = is.readBoolean();
            this.initialOutputWritten = is.readBoolean();
            this.scores = (List<Integer>) is.readObject();
            this.roundScores = (List<Integer>) is.readObject();
            for (int i = 0; i < 4; i++)
                this.chosenLists.add((List<Card>) is.readObject());
            is.close();
            fis.close();
            deleteFile("save_hearts");
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public void saveGame() {
        String filename = "save_hearts";
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
            objectStream.writeObject(scores);
            objectStream.writeObject(roundScores);
            for (int i = 0; i < 4; i++)
                objectStream.writeObject(chosenLists.get(i));
            outputStream.close();
            objectStream.close();
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
