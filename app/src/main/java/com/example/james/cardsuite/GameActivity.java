package com.example.james.cardsuite;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class GameActivity extends Activity implements Serializable {
    public static float sfxVolume = 0.5f, musicVolume = 0.5f;
    public static final int levelsToSearch = 3; //Parameter for AI that indicates how many levels down to search.
    protected Manager manager;
    protected int currentPlayerInteracting = 0, currentPotTurn = 0, firstNonBot = 0, lastNonBot = 0;
    //DP to pixel values to use...
    protected int cardWidthPX, cardHeightPX, cardDeltaXPX, bottomTopMarginPX, rightLeftMarginPX;
    private int soundsLoaded = 0;
    protected long lastClickTime = 0;
    protected boolean[] isBot = new boolean[4];
    protected boolean foundStartPlayer = false, finishedSwapping = false, buttonsPresent = false, finishedLoading = false, canClick = true;
    public boolean initialOutputWritten = false, isPaused = false, foundFirstNonBot = false;
    protected List<Integer> scores = new ArrayList<Integer>(), roundScores = new ArrayList<>();
    protected static final SoundPool[] soundPools = new SoundPool[] {new SoundPool.Builder().build(), new SoundPool.Builder().build(),
            new SoundPool.Builder().build(), new SoundPool.Builder().build(), new SoundPool.Builder().build(),
            new SoundPool.Builder().build()};
    protected int[] sounds;
    protected static final Random r = new Random();
    protected List<ImageView> cardViews = new ArrayList<ImageView>();
    protected static final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Play the sound of a card being played, unless it's hearts wherein it might be bid for swapping (different sound used).
        sounds = new int[] {soundPools[0].load(this,R.raw.cardplace1,1), soundPools[1].load(this,R.raw.cardplace2,1),
                soundPools[2].load(this, R.raw.cardplace3, 1), soundPools[3].load(this,R.raw.swapcardselect,1),
                soundPools[4].load(this,R.raw.dealcards,1), soundPools[5].load(this,R.raw.swapcardsaround,1)};

        SoundPool.OnLoadCompleteListener loadListener = new SoundPool.OnLoadCompleteListener() {
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

        //Converting the card dimensions in dp (35x50) into px; change to get dims of cards for entire app.
        //Also converting the distance between cards (60px) with dp.
        //These values were obtained using a OnePlus One (xxhdpi).
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        cardWidthPX = (int)Math.ceil(35 * metrics.density);
        cardHeightPX = (int)Math.ceil(50 * metrics.density);
        cardDeltaXPX = (int)Math.ceil(20 * metrics.density);
        bottomTopMarginPX = (int)Math.ceil(42 * metrics.density);
        rightLeftMarginPX = (int)Math.ceil(72 * metrics.density);
    }

    public void menuClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.pauseScreen));
        builder.setTitle("Game Paused");
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.pause_screen,null);
        builder.setView(view);
        builder.setCancelable(false);
        final AlertDialog dlg = builder.create();

        Button gameReturn = (Button) view.findViewById(R.id.return_game);
        Button displayScores = (Button) view.findViewById(R.id.display_scores);
        Button settings = (Button) view.findViewById(R.id.in_game_settings);
        Button exitMenu = (Button) view.findViewById(R.id.menu_button);

        gameReturn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        displayScores.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayScoreTable(null);
                dlg.dismiss();
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                runSettings();
            }
        });
        exitMenu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlg.dismiss();
                promptExit(v);
            }
        });
        dlg.show();

    }

    public void runSettings() {
        Intent intent = new Intent(GameActivity.this, com.example.james.cardsuite.SettingsActivity.class);
        startActivity(intent);
    }

    public void promptExit(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.pauseScreen));
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
    public void gameClick(final View v) {
        //If all sounds loaded, set the flag to true.
        if(soundsLoaded == sounds.length)
            finishedLoading = true;
    }

    public int getCardIndex(final View v) {
        int chosen = v.getId();
        for (int i = 0; i < currentPlayerInteracting; i++)
            chosen -= manager.getPlayers()[i].hand.size();
        return chosen;
    }

    // Called when a player places a valid card into the pot; updates the images in the pot
    public void displayPot() {
        for(int i = 0; i < manager.pot.size(); i++) {
            int index = (manager.startPlayer + i) % 4;
            ImageView potCard = null;
            switch(index) {
                case 0: potCard = (ImageView)findViewById(R.id.bottomPotCard); break;
                case 1: potCard = (ImageView)findViewById(R.id.leftPotCard); break;
                case 2: potCard = (ImageView)findViewById(R.id.topPotCard); break;
                case 3: potCard = (ImageView)findViewById(R.id.rightPotCard); break;
            }

            potCard.setImageResource(getResources().getIdentifier(manager.pot.get(index).getAddress(), "drawable", getPackageName()));
            potCard.setTag(getResources().getIdentifier(manager.pot.get(index).getAddress(), "drawable", getPackageName()));
            potCard.setMaxHeight(cardHeightPX);
            potCard.setElevation(i);
        }
    }

    // Called at the end of a round when all four players have added their cards; clears the pot using given IDs 100-103.
    public void potClear() {
        RelativeLayout potLayout = (RelativeLayout)findViewById(R.id.potLayout);
        for(int i = 0; i < 5; i++) {
            ImageView potView = (ImageView)potLayout.getChildAt(i);
            potView.setImageResource(0);
            potView.setTag(0);
        }
    }

    public void displayWaitScreen() {
        final Dialog alertDialog = new Dialog(this);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.wait_screen);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView text = (TextView) alertDialog.findViewById(R.id.wait_text);
        text.setText("Player " + (currentPlayerInteracting + 1) + ": Click anywhere to show current hand and continue the game.");
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                displayHands(currentPlayerInteracting, true);
            }
        });
        alertDialog.show();
    }

    public void displayScoreTable(final Runnable closeAction) {
        String[] column = { "Player 1", "Player 2", "Player 3", "Player 4" };
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
                }
                else if (j == 0) {
                    textView.setText(row.get(i - 1));
                    textView.setTypeface(null, Typeface.BOLD);
                }
                else if (i !=0 && j != 0)
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(sv);
        builder.setTitle("Scoreboard");
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(closeAction != null)
                    handler.post(closeAction);
            }
        });
        builder.show();
    }

    // reshuffles deck, increments round count, resets all variables for the next round.
    public abstract void reset();

    //Call when the hands have been updated and need be redisplayed.
    public abstract void displayHands(int player, boolean cardsClickable);

    //Call when the end piles and the scores displayed on top of the piles need be redisplayed.
    public abstract void displayEndPiles(List<Integer> scores);

    //Opens the guess dialog - fit for German Bridge for now.
    public abstract void openGuessDialog(final int currentPlayer);

    public abstract void dealCards();

    public void displayIntermediateHands(int numCardsToDisplay) {
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
            float initialTheta = (float) -2.25 * numCardsToDisplay / 2;
            for (int j = 0; j < numCardsToDisplay; j++) {
                float theta = (float) (initialTheta + 2.25 * j);
                deltaY = (int) (1.2 * (17.5 - Math.pow(j - numCardsToDisplay / 2, 2))); //Truncate the result of the offset

                if(numCardsToDisplay % 2 != 0 && j == (numCardsToDisplay-1)/2)
                    theta = 0;

                RelativeLayout.LayoutParams restParams = new RelativeLayout.LayoutParams(cardWidthPX, cardHeightPX);

                //How to treat and initialize the other cards depending on whether the current player or any other.
                ImageView cardButton = new ImageView(this);
                cardButton.setImageResource(getResources().getIdentifier("cardback", "drawable", getPackageName()));

                cardButton.setTag(manager.getPlayers()[i].hand.get(j));
                cardButton.setPadding(1, 1, 1, 1);
                cardButton.setScaleType(ImageView.ScaleType.FIT_CENTER);

                switch (i) {
                    case 0:
                        restParams.setMargins(deltaX,bottomTopMarginPX-deltaY, 0, 0);
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
                        top.addView(cardButton, restParams); break;
                    case 3:
                        restParams.setMargins(rightLeftMarginPX-deltaY, deltaX, 0, 0);
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

    public void loadGame(ObjectInputStream is) {
        try {
            this.currentPlayerInteracting = is.readInt();
            this.currentPotTurn = is.readInt();
            this.firstNonBot = is.readInt();
            this.lastNonBot = is.readInt();
            this.foundStartPlayer = is.readBoolean();
            this.finishedSwapping = is.readBoolean();
            this.buttonsPresent = is.readBoolean();
            this.initialOutputWritten = is.readBoolean();
            this.isBot = (boolean[]) is.readObject();
            this.scores = (List<Integer>) is.readObject();
            this.roundScores = (List<Integer>) is.readObject();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void saveGame(ObjectOutputStream objectStream) {
        try {
            objectStream.writeInt(currentPlayerInteracting);
            objectStream.writeInt(currentPotTurn);
            objectStream.writeInt(firstNonBot);
            objectStream.writeInt(lastNonBot);
            objectStream.writeBoolean(foundStartPlayer);
            objectStream.writeBoolean(finishedSwapping);
            objectStream.writeBoolean(buttonsPresent);
            objectStream.writeBoolean(initialOutputWritten);
            objectStream.writeObject(isBot);
            objectStream.writeObject(scores);
            objectStream.writeObject(roundScores);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public View findViewByCard(Card card) {
        for(ImageView view : cardViews) {
            if(((Card)view.getTag()).equals(card))
                return view;
        }
        return null;
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
