package com.gtjgroup.cardsuite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Intent intent = getIntent();
        final Manager manager = (Manager)intent.getSerializableExtra("manager");
        int gameMode = (Integer)intent.getSerializableExtra("game_mode");

        TextView resultsView = (TextView)findViewById(R.id.resultsView);
        if(gameMode == 2)
            resultsView.setText("Players " +(manager.findWinner()+1) + " and " + ((manager.findWinner()+2)%4+1) + " won the game!");
        else
            resultsView.setText("Player " + (manager.findWinner()+1) + " won the game!");

        final ImageButton playButton = (ImageButton) findViewById(R.id.returntomenu);
        playButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView iv = (ImageView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    iv.setAlpha(0.5f);
                    newGameClick(v);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    iv.setAlpha(1f);
                    return true;
                }

                return false;
            }
        });
        final ImageButton scoresButton = (ImageButton) findViewById(R.id.display_scores);
        scoresButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView iv = (ImageView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    displayScoreTable(manager);
                    iv.setAlpha(0.5f);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    iv.setAlpha(1f);
                    return true;
                }

                return false;
            }
        });

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("75323E59FF3A690DE8299B8B05344B0C")
                .build();
        mAdView.loadAd(adRequest);
    }

    public void displayScoreTable(Manager manager) {
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

        int currentAPILevel = android.os.Build.VERSION.SDK_INT;
        if(currentAPILevel > android.os.Build.VERSION_CODES.KITKAT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.BidCustom));
            builder.setCancelable(false);
            builder.setView(sv);
            builder.setTitle("Scoreboard");
            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        } else {
            android.support.v7.app.AlertDialog.Builder builder =
                    new android.support.v7.app.AlertDialog.Builder(this, R.style.BidCustom);
            builder.setCancelable(false);
            builder.setView(sv);
            builder.setTitle("Scoreboard");
            builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }
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
