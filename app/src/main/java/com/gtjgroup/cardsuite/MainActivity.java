package com.gtjgroup.cardsuite;

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
    static final String FONT_TYPE = "SigniaPro-Regular.ttf";
    static final float BUTTON_DOWN_OPACITY = 0.5f, BUTTON_OPACITY = 1f;
    boolean[] isBot = new boolean[4];
    boolean loadGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_main);

        //Load the ad first.
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(ApplicationID.AD_ID)
                .build();
        mAdView.loadAd(adRequest);

        //Set the background music.
        SoundManager.prepare(this);
        if (!SoundManager.isPlayingBGM())
            SoundManager.playBackgroundMusic(this);

        Typeface font = Typeface.createFromAsset(getAssets(), FONT_TYPE);
        TextView greetingView = (TextView)findViewById(R.id.newgreeting);
        greetingView.setTypeface(font);

        //Initialize the buttons with their touch listeners.
        //Play brings up an overlay of games to choose from.
        //TODO - If we decide to support API level 24, we can replace the below 4 listeners with lambda exprs/functors to save lines.
        final ImageButton playButton = (ImageButton) findViewById(R.id.play);
        final ImageButton settingsButton = (ImageButton) findViewById(R.id.settings_button);
        final ImageButton helpButton = (ImageButton) findViewById(R.id.help_button);
        final TextView creditsButton = (TextView) findViewById(R.id.credits);

        playButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView iv = (ImageView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    overlayClick(v);
                    SoundManager.playButtonClickSound();
                    iv.setAlpha(BUTTON_DOWN_OPACITY);
                } else if (event.getAction() == MotionEvent.ACTION_UP)
                    iv.setAlpha(BUTTON_OPACITY);
                else
                    return false;

                return true;
            }
        });

        settingsButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView iv = (ImageView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    settingsClick(v);
                    SoundManager.playButtonClickSound();
                    iv.setAlpha(BUTTON_DOWN_OPACITY);
                } else if (event.getAction() == MotionEvent.ACTION_UP)
                    iv.setAlpha(BUTTON_OPACITY);
                else
                    return false;

                return true;
            }
        });

        helpButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView iv = (ImageView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    helpClick(v);
                    SoundManager.playButtonClickSound();
                    iv.setAlpha(BUTTON_DOWN_OPACITY);
                } else if (event.getAction() == MotionEvent.ACTION_UP)
                    iv.setAlpha(BUTTON_OPACITY);
                else
                    return false;

                return true;
            }
        });

        creditsButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextView iv = (TextView) v;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    creditsClick(v);
                    SoundManager.playButtonClickSound();
                    iv.setAlpha(BUTTON_DOWN_OPACITY);
                } else if (event.getAction() == MotionEvent.ACTION_UP)
                    iv.setAlpha(BUTTON_OPACITY);
                else
                    return false;

                return true;
            }
        });
    }

    @Override
    public void onPause() {
        if (SoundManager.isPlayingBGM())
            SoundManager.stopBackgroundMusic();
        super.onStop();
    }

    @Override
    public void onResume() {
        if (!SoundManager.isPlayingBGM())
            SoundManager.playBackgroundMusic(this);
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }

    //Given the game mode selected, load a previous game if its corresponding file exists or star
    public void gameClick(int gameMode) {
        if((gameMode == 0 && (fileExists(this, "save_hearts"))) ||
                (gameMode == 1 && (fileExists(this, "save_bridge"))) ||
                (gameMode == 2 && (fileExists(this, "save_spades"))))
            savedGamePrompt(gameMode);
        else
            playerSelection(gameMode);
    }

    public void settingsClick(View v) {
        SoundManager.playButtonClickSound();
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
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
        bridgeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SoundManager.playButtonClickSound();
                alertDialog.dismiss();
                gameClick(1);
            }
        });
        ImageButton spadesButton = (ImageButton) alertDialog.findViewById(R.id.spades_button);
        spadesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SoundManager.playButtonClickSound();
                alertDialog.dismiss();
                gameClick(2);
            }
        });
    }

    public void creditsClick(View v) {
        SoundManager.playButtonClickSound();
        Intent intent = new Intent(this,CreditsActivity.class);
        startActivity(intent);
        finish();
    }

    public void savedGamePrompt(final int gameMode) {
        int currentAPILevel = android.os.Build.VERSION.SDK_INT;
        //Note, these are 2 distinct classes and share no common base class besides Object, so it looks redundant.
        if(currentAPILevel > android.os.Build.VERSION_CODES.KITKAT) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyAppTheme));
            builder.setTitle(R.string.continue_from_previous_prompt);
            builder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SoundManager.playButtonClickSound();
                    loadGame = true;
                    runGameActivity(gameMode);
                }
            });
            builder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SoundManager.playButtonClickSound();
                    loadGame = false;
                    playerSelection(gameMode);
                }
            });
            builder.show();
        } else {
            android.support.v7.app.AlertDialog.Builder builder =
                    new android.support.v7.app.AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyAppTheme));
            builder.setTitle(R.string.continue_from_previous_prompt);
            builder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SoundManager.playButtonClickSound();
                    loadGame = true;
                    runGameActivity(gameMode);
                }
            });
            builder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SoundManager.playButtonClickSound();
                    loadGame = false;
                    playerSelection(gameMode);
                }
            });
            builder.show();
        }
        onPause();
    }

    public void playerSelection(final int gameMode) {
        //In case we wish to implement player names, we can change them from this array.
        final String[] choices = {"PLAYER 2", "PLAYER 3", "PLAYER 4", "PLAYER 1"};
        ListView players = new ListView(this);
        int currentAPILevel = android.os.Build.VERSION.SDK_INT;

        isBot[0] = false;
        isBot[1] = isBot[2] = isBot[3] = true;

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row, choices) {
            int selectedPosition = -1;

            //Select which players should be bots.
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

                if(position != 0)
                    botButton.setChecked(true);

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

        players.setAdapter(adapter);

        DialogInterface.OnClickListener okClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SoundManager.playButtonClickSound();
                runGameActivity(gameMode);
            }
        }, backClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SoundManager.playButtonClickSound();
                dialog.cancel();
            }
        };

        //To maintain the dialog look when possible, use a different builder depending on OS version.
        //Similar to above, the builders share no feasible common base class; I feel this form is less convoluted.
        if(currentAPILevel > android.os.Build.VERSION_CODES.KITKAT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyAppTheme));
            builder.setView(players);
            builder.setTitle(R.string.select_players);
            builder.setPositiveButton(R.string.ok, okClickListener);
            builder.setNeutralButton(R.string.back, backClickListener);
            builder.setCancelable(false);
            builder.show();
        } else {
            android.support.v7.app.AlertDialog.Builder builder =
                    new android.support.v7.app.AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyAppTheme));
            builder.setView(players);
            builder.setTitle(R.string.select_players);
            builder.setPositiveButton(R.string.ok, okClickListener);
            builder.setNeutralButton(R.string.back, backClickListener);
            builder.setCancelable(false);
            builder.show();
        }
    }

    public void runGameActivity(int gameMode) {
        Class<? extends GameActivity> executingActivity = null;
        switch(gameMode) {
            case 0: executingActivity = HeartsActivity.class; break;
            case 1: executingActivity = BridgeActivity.class; break;
            case 2: executingActivity = SpadesActivity.class; break;
        }

        SoundManager.stopBackgroundMusic();
        Intent intent = new Intent(MainActivity.this,executingActivity);
        intent.putExtra("isBot", isBot);
        intent.putExtra("loadGame", loadGame);
        startActivity(intent);
        finish();
    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        return file != null && file.exists();
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
