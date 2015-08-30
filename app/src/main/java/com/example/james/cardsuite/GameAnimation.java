package com.example.james.cardsuite;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class GameAnimation {
    public static void placeCard(Activity activity, View v, int player) {
        int[] potCoordinates = new int[2], cardCoordinates = new int[2];
        v.getLocationOnScreen(cardCoordinates);
        TranslateAnimation ta;
        switch(player) {
            case 0: activity.findViewById(R.id.bottomPotCard).getLocationOnScreen(potCoordinates); break;
            case 1: activity.findViewById(R.id.leftPotCard).getLocationOnScreen(potCoordinates); break;
            case 2: activity.findViewById(R.id.topPotCard).getLocationOnScreen(potCoordinates); break;
            case 3: activity.findViewById(R.id.rightPotCard).getLocationOnScreen(potCoordinates); break;
        }
        ta = new TranslateAnimation(0,potCoordinates[0]-cardCoordinates[0],0,potCoordinates[1]-cardCoordinates[1]);
        ta.setDuration(150);
        v.startAnimation(ta);
    }

    public static void collectEndPile(Activity activity, int winningPlayer) {
        int[] potCoordinates = new int[2], pileCoordinates = new int[2];

        switch(winningPlayer) {
            case 0: activity.findViewById(R.id.bottomPile).getLocationOnScreen(pileCoordinates); break;
            case 1: activity.findViewById(R.id.leftPile).getLocationOnScreen(pileCoordinates); break;
            case 2: activity.findViewById(R.id.topPile).getLocationOnScreen(pileCoordinates); break;
            case 3: activity.findViewById(R.id.rightPile).getLocationOnScreen(pileCoordinates); break;
        }

        RelativeLayout potLayout = (RelativeLayout)activity.findViewById(R.id.potLayout);

        for(int i = 0; i < potLayout.getChildCount(); i++) {
            View v = potLayout.getChildAt(i);
            if(v.getTag() == (Integer)0)
                continue;
            v.getLocationOnScreen(potCoordinates);

            TranslateAnimation ta = new TranslateAnimation(0,potCoordinates[0]-pileCoordinates[0],0,potCoordinates[1]-pileCoordinates[1]);
            ta.setDuration(200);
            v.startAnimation(ta);
        }
    }
}
