package com.example.james.cardsuite;

import android.app.Activity;
import android.view.View;
import android.view.animation.TranslateAnimation;

public class GameAnimation {
    public static void placeCard(Activity activity, View v, int player) {
        int[] potCoordinates = new int[2], cardCoordinates = new int[2];
        v.getLocationOnScreen(cardCoordinates);
        TranslateAnimation ta;
        switch(player) {
            case 0: activity.findViewById(R.id.bottomPotCard).getLocationOnScreen(potCoordinates); break;
            case 1: activity.findViewById(R.id.leftPotCard).getLocationInWindow(potCoordinates); break;
            case 2: activity.findViewById(R.id.topPotCard).getLocationInWindow(potCoordinates); break;
            case 3: activity.findViewById(R.id.rightPotCard).getLocationInWindow(potCoordinates); break;
        }
        ta = new TranslateAnimation(0,potCoordinates[0]-cardCoordinates[0],0,potCoordinates[1]-cardCoordinates[1]);
        ta.setDuration(200);
        v.startAnimation(ta);

    }
}
