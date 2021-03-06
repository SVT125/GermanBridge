package com.gtjgroup.cardsuite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import java.util.Arrays;
import java.util.Random;

public class SoundManager {
    public static float sfxVolume = 0.5f, musicVolume = 0.5f;
    protected static SoundPool[] soundPools = new SoundPool[7];
    protected static int[] sounds;
    public static MediaPlayer player = null;
    private static int soundsLoaded = 0;
    private static boolean finishedLoading = false, playingBGM = false;
    protected static final Random r = new Random();

    @SuppressLint("NewApi")
    public static void prepare(Activity activity) {
        Context context = activity.getApplicationContext();

        int currentAPILevel = Build.VERSION.SDK_INT;
        if(currentAPILevel > Build.VERSION_CODES.KITKAT)
            Arrays.fill(soundPools, new SoundPool.Builder().build());
        else
            Arrays.fill(soundPools, new SoundPool(1, AudioManager.STREAM_MUSIC,0));

        //Play the sound of a card being played, unless it's hearts wherein it might be bid for swapping (different sound used).
        sounds = new int[] {soundPools[0].load(context,R.raw.cardplace1,1), soundPools[1].load(context,R.raw.cardplace2,1),
                soundPools[2].load(context, R.raw.cardplace3, 1), soundPools[3].load(context,R.raw.swapcardselect,1),
                soundPools[4].load(context,R.raw.dealcards,1), soundPools[5].load(context,R.raw.swapcardsaround,1),
                soundPools[6].load(context,R.raw.buttonclick,1)};

        SoundPool.OnLoadCompleteListener loadListener = new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundsLoaded++;
            }
        };

        for(SoundPool pool : soundPools)
            pool.setOnLoadCompleteListener(loadListener);
    }

    public static void playBackgroundMusic(Activity activity) {
        if (player == null) {
            player = MediaPlayer.create(activity, R.raw.easylemon);
            player.setLooping(true);
        }
        player.setVolume(musicVolume,musicVolume);
        player.start();
        playingBGM = true;
    }

    public static void stopBackgroundMusic() {
        player.pause();
        playingBGM = false;
    }

    public static void endPlayer() {
        player.stop();
        player.release();
        player = null;
    }

    public static boolean isLoaded() {
        if(soundsLoaded == sounds.length)
            finishedLoading = true;

        return finishedLoading;
    }

    public static boolean isPlayingBGM() {
        return playingBGM;
    }

    public static void playPlaceCardSound() {
        int chosenSound = r.nextInt(3);
        soundPools[chosenSound].play(sounds[chosenSound], sfxVolume, sfxVolume, 0, 0, 1);
    }

    public static void playDealCardSound() {
        soundPools[4].play(sounds[4], sfxVolume, sfxVolume, 0, 1, 1);
    }

    public static void playSwapSelectSound() {
        soundPools[3].play(sounds[3], sfxVolume, sfxVolume, 0, 0, 1);
    }

    public static void playSwappingSound() {
        soundPools[5].play(sounds[5], sfxVolume, sfxVolume, 0, 1, 1);
    }

    public static void playButtonClickSound() {
        soundPools[6].play(sounds[6], sfxVolume * 0.5f, sfxVolume * 0.5f, 0, 0, 1);
    }
}
