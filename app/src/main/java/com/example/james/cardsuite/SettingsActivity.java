package com.example.james.cardsuite;

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
                    GameActivity.musicVolume = ((int) newValue) / 100f;
                    return true;
                }
            });

            SeekbarPreference SFXPreference = (SeekbarPreference)findPreference("master_sfx_settings");
            SFXPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    GameActivity.sfxVolume = ((int) newValue) / 100f;
                    return true;
                }
            });
        }

    }

}
