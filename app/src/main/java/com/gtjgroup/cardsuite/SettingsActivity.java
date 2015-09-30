package com.gtjgroup.cardsuite;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsPage()).commit();
    }

    @Override
    public void onPause() {
        if (SoundManager.isPlayingBGM())
            SoundManager.stopBackgroundMusic();
        super.onPause();
    }

    public static class SettingsPage extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);


            SeekbarPreference musicPreference = (SeekbarPreference)findPreference("master_music_settings");
            musicPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SoundManager.musicVolume = ((int) newValue) / 100f;
                    return true;
                }
            });
            SeekbarPreference SFXPreference = (SeekbarPreference)findPreference("master_sfx_settings");
            SFXPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SoundManager.sfxVolume = ((int) newValue) / 100f;
                    return true;
                }
            });
            SeekbarPreference gameSpeedPreference = (SeekbarPreference)findPreference("game_speed_settings");
            gameSpeedPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    //The range of game speed as represented by delays is [0-2000] ms.
                    GameActivity.gameSpeedRange = 20*(Math.abs((int)newValue-100));
                    return true;
                }
            });
            TextPreference nameOne = (TextPreference)findPreference("player_one_text");
            TextPreference nameTwo = (TextPreference)findPreference("player_two_text");
            TextPreference nameThree = (TextPreference)findPreference("player_three_text");
            TextPreference nameFour = (TextPreference)findPreference("player_four_text");
            nameOne.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    GameActivity.playerNames[0] = (String)newValue;
                    return true;
                }
            });
            nameTwo.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    GameActivity.playerNames[1] = (String)newValue;
                    return true;
                }
            });
            nameThree.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    GameActivity.playerNames[2] = (String)newValue;
                    return true;
                }
            });
            nameFour.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    GameActivity.playerNames[3] = (String)newValue;
                    return true;
                }
            });
        }

    }

}
