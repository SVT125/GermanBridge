package com.example.james.cardsuite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends Activity {

    boolean[] isBot = new boolean[4];
    boolean loadGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_main);

        Typeface font = Typeface.createFromAsset(getAssets(), "SigniaPro-Regular.ttf");
        TextView greetingView = (TextView)findViewById(R.id.newgreeting);
        greetingView.setTypeface(font);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    public void gameClick(final View v) {
        if (v == findViewById(R.id.hearts_button) && (fileExists(this, "save_hearts")))
            savedGamePrompt(v);
        else if (v == findViewById(R.id.german_button) && (fileExists(this, "save_bridge")))
            savedGamePrompt(v);
        else if (v == findViewById(R.id.spades_button) && (fileExists(this, "save_spades")))
            savedGamePrompt(v);
        else
            playerSelection(v);
    }

    public void settingsClick(View v) {
        Intent intent = new Intent(MainActivity.this, com.example.james.cardsuite.SettingsActivity.class);
        startActivity(intent);
    }

    public void overlayClick(View v) {
        View view = findViewById(R.id.overlay);
        view.setVisibility(0);
    }

    public void savedGamePrompt(final View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.MyAppTheme));
        builder.setTitle("Do you want to continue from a previous game?");
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadGame = true;
                runGameActivity(v);
            }
        });
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadGame = false;
                playerSelection(v);
            }
        });
        builder.show();
        onPause();
    }

    public void playerSelection(final View v) {
        final String[] choices = new String[3];

        choices[0] = "PLAYER 2";
        choices[1] = "PLAYER 3";
        choices[2] = "PLAYER 4";

        isBot[0] = false;
        isBot[1] = isBot[2] = isBot[3] = true;

        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.row, choices) {

            int selectedPosition = -1;

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.row, null);
                }
                TextView name = (TextView) v.findViewById(R.id.textView1);
                name.setText(choices[position]);
                RadioButton playerButton = (RadioButton)v.findViewById(R.id.playerButton);
                RadioButton botButton = (RadioButton)v.findViewById(R.id.botButton);

                playerButton.setTag(position);
                playerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedPosition = (Integer)view.getTag();
                        isBot[selectedPosition + 1] = false;
                    }
                });

                if(position != 0) {
                    botButton.setChecked(true);
                }

                botButton.setTag(position);
                botButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedPosition = (Integer)view.getTag();
                        isBot[selectedPosition + 1] = true;
                    }
                });
                return v;
            }
        };


        ListView players = new ListView(this);
        players.setAdapter(adapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyAppTheme));
        builder.setView(players);
        builder.setTitle("Select players or bots");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runGameActivity(v);
            }
        });
        builder.setNeutralButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public void runGameActivity(View v) {
        Class<? extends GameActivity> executingActivity = null;
        if(v == findViewById(R.id.hearts_button))
            executingActivity = HeartsActivity.class;
        else if(v == findViewById(R.id.german_button))
            executingActivity = BridgeActivity.class;
        else if(v == findViewById(R.id.spades_button))
            executingActivity = SpadesActivity.class;

        Intent intent = new Intent(MainActivity.this,executingActivity);
        intent.putExtra("isBot", isBot);
        intent.putExtra("loadGame", loadGame);
        startActivity(intent);
        finish();
    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }

    public static class SettingsActivity extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
