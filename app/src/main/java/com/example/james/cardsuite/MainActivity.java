package com.example.james.cardsuite;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;

public class MainActivity extends Activity {
    boolean[] isBot = new boolean[4];
    boolean loadGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_main);

        SoundManager.prepare(this);

        Typeface font = Typeface.createFromAsset(getAssets(), "SigniaPro-Regular.ttf");
        TextView greetingView = (TextView)findViewById(R.id.newgreeting);
        greetingView.setTypeface(font);

        final ImageButton playButton = (ImageButton) findViewById(R.id.play);
        playButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView iv = (ImageView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    iv.setAlpha(0.5f);
                    SoundManager.playButtonClickSound();
                    overlayClick(v);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    iv.setAlpha(1f);
                    return true;
                }

                return false;
            }
        });
        final ImageButton settingsButton = (ImageButton) findViewById(R.id.settings_button);
        settingsButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView iv = (ImageView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    settingsClick(v);
                    SoundManager.playButtonClickSound();
                    iv.setAlpha(0.5f);
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    iv.setAlpha(1f);
                    return true;
                }

                return false;
            }
        });
        final ImageButton helpButton = (ImageButton) findViewById(R.id.help_button);
        helpButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView iv = (ImageView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    helpClick(v);
                    SoundManager.playButtonClickSound();
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
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    public void gameClick(int gameMode) {
        if (gameMode == 0 && (fileExists(this, "save_hearts")))
            savedGamePrompt(0);
        else if (gameMode == 1 && (fileExists(this, "save_bridge")))
            savedGamePrompt(1);
        else if (gameMode == 2 && (fileExists(this, "save_spades")))
            savedGamePrompt(2);
        else
            playerSelection(gameMode);
    }

    public void settingsClick(View v) {
        SoundManager.playButtonClickSound();
        Intent intent = new Intent(MainActivity.this, com.example.james.cardsuite.SettingsActivity.class);
        startActivity(intent);
    }

    public void helpClick(View v) {
        SoundManager.playButtonClickSound();
        startActivity(new Intent(this, PageViewActivity.class));
    }

    public void overlayClick(View v) {
        final Dialog alertDialog = new Dialog(this);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.menu_overlay);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        ImageButton heartsButton = (ImageButton) alertDialog.findViewById(R.id.hearts_button);
        heartsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SoundManager.playButtonClickSound();
                alertDialog.dismiss();
                gameClick(0);
            }
        });
        ImageButton bridgeButton = (ImageButton) alertDialog.findViewById(R.id.german_button);
        bridgeButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                SoundManager.playButtonClickSound();
                alertDialog.dismiss();
                gameClick(1);
            }
        });
        ImageButton spadesButton = (ImageButton) alertDialog.findViewById(R.id.spades_button);
        spadesButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                SoundManager.playButtonClickSound();
                alertDialog.dismiss();
                gameClick(2);
            }
        });
    }

    public void savedGamePrompt(final int gameMode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.MyAppTheme));
        builder.setTitle("Do you want to continue from a previous game?");
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SoundManager.playButtonClickSound();
                loadGame = true;
                runGameActivity(gameMode);
            }
        });
        builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SoundManager.playButtonClickSound();
                loadGame = false;
                playerSelection(gameMode);
            }
        });
        builder.show();
        onPause();
    }

    public void playerSelection(final int gameMode) {
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
                SoundManager.playButtonClickSound();
                runGameActivity(gameMode);
            }
        });
        builder.setNeutralButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SoundManager.playButtonClickSound();
                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public void runGameActivity(int gameMode) {
        Class<? extends GameActivity> executingActivity = null;
        if(gameMode == 0)
            executingActivity = HeartsActivity.class;
        else if(gameMode == 1)
            executingActivity = BridgeActivity.class;
        else if(gameMode == 2)
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
