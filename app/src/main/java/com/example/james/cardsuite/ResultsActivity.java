package com.example.james.cardsuite;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ResultsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Intent intent = getIntent();
        Manager manager = (Manager)intent.getSerializableExtra("manager");
        Player[] players = (Player[])intent.getSerializableExtra("players");
        int[] scores = (int[])intent.getSerializableExtra("scores");
        int gameMode = (Integer)intent.getSerializableExtra("game_mode");
        manager.players = players;
        for(int i = 0; i < 4; i++)
            manager.players[i].score = scores[i];

        TextView resultsView = (TextView)findViewById(R.id.resultsView);
        if(gameMode == 2)
            resultsView.setText("Players " +(manager.findWinner()+1) + " and " + ((manager.findWinner()+2)%4+1) + " won the game!");
        else
            resultsView.setText("Player " + (manager.findWinner()+1) + " won the game!");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    // Starts a new game when clicked - goes to the main activity again.
    public void newGameClick(View v) {
        Intent intent = new Intent(ResultsActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_results, menu);
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
