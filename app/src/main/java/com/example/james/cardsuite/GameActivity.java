package com.example.james.cardsuite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class GameActivity extends Activity {
    protected Manager manager;
    protected int currentPlayerInteracting = 0;
    protected int currentPotTurn = 0;
    private int soundsLoaded = 0;
    public final int levelsToSearch = 3; //Parameter for AI that indicates how many levels down to search.
    protected boolean[] isBot = new boolean[4];
    protected boolean foundStartPlayer = false, finishedSwapping = false, buttonsPresent = false, finishedLoading = false;
    public boolean initialOutputWritten = false;
    protected List<Integer> scores = new ArrayList<Integer>(), roundScores = new ArrayList<>();
    protected SoundPool[] soundPools = new SoundPool[] {new SoundPool.Builder().build(), new SoundPool.Builder().build(),
            new SoundPool.Builder().build(), new SoundPool.Builder().build()};
    protected int[] sounds;
    protected SoundPool.OnLoadCompleteListener loadListener;
    protected Random r = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    // reshuffles deck, increments round count, resets all variables for the next round.
    public abstract void reset();

    //Call when the hands have been updated and need be redisplayed.
    public abstract void displayHands(int player);

    //Call when the end piles and the scores displayed on top of the piles need be redisplayed.
    public abstract void displayEndPiles(List<Integer> scores);

    //Opens the guess dialog - fit for German Bridge for now.
    public abstract void openGuessDialog(final int currentPlayer);

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
