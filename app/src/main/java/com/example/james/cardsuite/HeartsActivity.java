package com.example.james.cardsuite;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.res.Configuration;
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

public class HeartsActivity extends GameActivity {
    private boolean finishedSwapping = false;
    private List<List<Card>> chosenLists = new ArrayList<List<Card>>();
    private List<Integer> chosenIndices = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearts);

        Intent intent = getIntent();
        this.isSinglePlayer = intent.getBooleanExtra("isSinglePlayer", true);

        manager = new HeartsManager();

        //Display the image buttons
        displayHands(0);
        displayEndPiles(scores);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    public void gameClick(View v) {
        super.gameClick(v);

        //Play sounds only if we're done swapping in hearts or are in any other game mode.
        if((finishedLoading && finishedSwapping)) {
            int chosenSound = r.nextInt(3);
            soundPools[chosenSound].play(sounds[chosenSound],1,1,0,0,1);
        }

        if(!(manager.isGameOver())) {
            int chosen = v.getId();
            for(int i = 0; i < currentPlayerInteracting; i++)
                chosen -= manager.getPlayers()[i].hand.size();

            Card chosenCard = manager.getPlayers()[currentPlayerInteracting].hand.get(chosen);

            //Select or unselect the card with a border shown
            if(chosenCard.isClicked == false) {

                switch(currentPlayerInteracting){
                    case 0:
                        AnimatorSet selected_f = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.selected_forward);
                        selected_f.setTarget(v);
                        selected_f.start();
                        break;
                    case 1:
                        AnimatorSet selected_r = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.selected_right);
                        selected_r.setTarget(v);
                        selected_r.start();
                        break;
                    case 2:
                        AnimatorSet selected_b = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.selected_back);
                        selected_b.setTarget(v);
                        selected_b.start();
                        break;
                    case 3:
                        AnimatorSet selected_l = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.selected_left);
                        selected_l.setTarget(v);
                        selected_l.start();
                        break;
                }
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

                displayEndPiles(roundScores);
                manager.usedCards.addAll(manager.pot.values());
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
                displayEndPiles(roundScores);
                reset();
                return;
            }
        } else {
            // The game is done - pass all relevant information for results activity to display.
            // Passing manager just in case for future statistics if needbe.
            Intent intent = new Intent(HeartsActivity.this, ResultsActivity.class);
            intent.putExtra("manager", manager);
            intent.putExtra("players", manager.getPlayers());
            startActivity(intent);
            finish();
        }
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

    //Call when the end piles and the scores displayed on top of the piles need be redisplayed.
    public void displayEndPiles(List<Integer> scores) {
        TextView[] scoreViews = new TextView[] {(TextView)findViewById(R.id.bottomScore), (TextView)findViewById(R.id.leftScore),
                (TextView)findViewById(R.id.topScore), (TextView)findViewById(R.id.rightScore)};
        ImageView[] pileViews = new ImageView[] {(ImageView)findViewById(R.id.bottomPile), (ImageView)findViewById(R.id.leftPile),
                (ImageView)findViewById(R.id.topPile), (ImageView)findViewById(R.id.rightPile)};

        for(int i = 0; i < 4; i++) {
            //Update the score, but remove or update the pile if it exists.
            if(scores.get(i) != 0) {
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

    //Opens the guess dialog - inapplicable for hearts however, refactoring can be done to differentiate hearts from other games.
    public void openGuessDialog(final int currentPlayer) {}

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
