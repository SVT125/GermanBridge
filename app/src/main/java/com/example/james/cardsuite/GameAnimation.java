package com.example.james.cardsuite;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class GameAnimation {
    public static void placeCard(GameActivity activity, View v, int player) {
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

    public static void collectEndPile(GameActivity activity, int winningPlayer) {
        int[] pileCoordinates = new int[2];

        switch(winningPlayer) {
            case 0: activity.findViewById(R.id.bottomPile).getLocationOnScreen(pileCoordinates); break;
            case 1: activity.findViewById(R.id.leftPile).getLocationOnScreen(pileCoordinates); break;
            case 2: activity.findViewById(R.id.topPile).getLocationOnScreen(pileCoordinates); break;
            case 3: activity.findViewById(R.id.rightPile).getLocationOnScreen(pileCoordinates); break;
        }

        for(int i = 0; i < 4; i++) {
            int[] potCoordinates = new int[2];
            View v = null;
            switch(i) {
                case 0: v = activity.findViewById(R.id.bottomPotCard); break;
                case 1: v = activity.findViewById(R.id.leftPotCard); break;
                case 2: v = activity.findViewById(R.id.topPotCard); break;
                case 3: v = activity.findViewById(R.id.rightPotCard); break;
            }

            if(v.getTag() == (Integer)0)
                continue;
            v.getLocationOnScreen(potCoordinates);

            TranslateAnimation ta = new TranslateAnimation(0,pileCoordinates[0]-potCoordinates[0],0,pileCoordinates[1]-potCoordinates[1]);
            ta.setDuration(250);
            v.startAnimation(ta);
        }
    }

    public static void swapCards(HeartsActivity activity, int currentPlayer, int swapRound, View v, View v2, View v3) {
        int receivingHand = 0;
        switch(swapRound) {
            case 0: receivingHand = receivingHand == 0 ? 2 : receivingHand - 1; break;
            case 1: receivingHand = (receivingHand+1)%3; break;
            case 2: receivingHand = (receivingHand+2)%3; break;
        }

        int[] initialCoordinates, finalCoordinates;
        for(View view : new View[]{v,v2,v3}) {
            initialCoordinates = finalCoordinates = new int[2];
            view.getLocationOnScreen(initialCoordinates);
            switch(receivingHand) {
                case 0: activity.findViewById(R.id.bottomPlayerHandLayout).getLocationOnScreen(finalCoordinates); break;
                case 1: activity.findViewById(R.id.leftPlayerHandLayout).getLocationOnScreen(finalCoordinates); break;
                case 2: activity.findViewById(R.id.topPlayerHandLayout).getLocationOnScreen(finalCoordinates); break;
                case 3: activity.findViewById(R.id.rightPlayerHandLayout).getLocationOnScreen(finalCoordinates); break;
            }

            TranslateAnimation ta = new TranslateAnimation(0,finalCoordinates[0]-initialCoordinates[0],0,finalCoordinates[1]-initialCoordinates[1]);
            ta.setDuration(250);
            view.startAnimation(ta);
        }
    }

    // Deals one card to each of the 4 players at the same time.
    public static void dealSingleCards(GameActivity activity, int[] initialCoordinates) {
        int[][] finalCoordinatesArray = new int[][] {new int[2], new int[2], new int[2], new int[2]};
        activity.findViewById(R.id.bottomPlayerHandLayout).getLocationOnScreen(finalCoordinatesArray[0]);
        finalCoordinatesArray[0][0] += activity.findViewById(R.id.bottomPlayerHandLayout).getWidth()/2;
        activity.findViewById(R.id.leftPlayerHandLayout).getLocationOnScreen(finalCoordinatesArray[1]);
        finalCoordinatesArray[1][0] += activity.findViewById(R.id.leftPlayerHandLayout).getWidth();
        finalCoordinatesArray[1][1] += activity.findViewById(R.id.leftPlayerHandLayout).getHeight()/2;
        activity.findViewById(R.id.topPlayerHandLayout).getLocationOnScreen(finalCoordinatesArray[2]);
        finalCoordinatesArray[2][0] += activity.findViewById(R.id.topPlayerHandLayout).getWidth()/2;
        finalCoordinatesArray[2][1] += activity.findViewById(R.id.topPlayerHandLayout).getHeight();
        activity.findViewById(R.id.rightPlayerHandLayout).getLocationOnScreen(finalCoordinatesArray[3]);
        finalCoordinatesArray[3][1] += activity.findViewById(R.id.rightPlayerHandLayout).getHeight()/2;

        for(int[] finalCoordinates : finalCoordinatesArray) {
            final ImageView imageView = new ImageView(activity.getApplicationContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

            imageView.setLayoutParams(params);
            imageView.setImageResource(activity.getResources().getIdentifier("cardback", "drawable", activity.getPackageName()));
            imageView.setMaxHeight(115);
            imageView.setAdjustViewBounds(true);
            ((RelativeLayout) activity.findViewById(R.id.potLayout)).addView(imageView);

            imageView.animate().setDuration(100).translationXBy(finalCoordinates[0] - initialCoordinates[0])
                    .translationYBy(finalCoordinates[1] - initialCoordinates[1])
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            ((ViewGroup) imageView.getParent()).removeView(imageView);
                        }
                    }).start();
        }
    }
}
