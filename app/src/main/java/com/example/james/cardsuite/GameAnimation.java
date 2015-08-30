package com.example.james.cardsuite;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class GameAnimation {
    public static void placeCard(Activity activity, View v, int player) {
        int[] cardCoordinates = new int[2];
        TranslateAnimation ta;
        switch(player) {
            case 0: activity.findViewById(R.id.bottomPotCard).getLocationOnScreen(cardCoordinates);
                ta = new TranslateAnimation(0,cardCoordinates[0]-v.getX(),0,-(cardCoordinates[1]));
                ta.setDuration(250);
                v.startAnimation(ta); break;
            case 1: activity.findViewById(R.id.leftPotCard).getLocationInWindow(cardCoordinates);
                ta = new TranslateAnimation(0,2*(cardCoordinates[0]-v.getX()),0,(cardCoordinates[1]-v.getY()));
                ta.setDuration(250);
                v.startAnimation(ta); break;
            case 2: activity.findViewById(R.id.topPotCard).getLocationInWindow(cardCoordinates);
                ta = new TranslateAnimation(0,cardCoordinates[0]-v.getX(),0,-(cardCoordinates[1]-v.getY()));
                ta.setDuration(250);
                v.startAnimation(ta); break;
            case 3: activity.findViewById(R.id.rightPotCard).getLocationInWindow(cardCoordinates);
                ta = new TranslateAnimation(0,-(cardCoordinates[0]-v.getX()),0,(cardCoordinates[1]-v.getY()));
                ta.setDuration(250);
                v.startAnimation(ta); break;
        }

    }
}
