package com.example.james.cardsuite;

import android.os.Handler;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

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

    public static void dealCards(final GameActivity activity, Manager manager) {
        int[] initialCoordinates = new int[2];
        activity.findViewById(R.id.anchor).getLocationOnScreen(initialCoordinates);

        ImageView imageView = new ImageView(activity.getApplicationContext());
        imageView.setImageResource(activity.getResources().getIdentifier("cardback", "drawable", activity.getPackageName()));

        for(int i = 0; i < 4; i++) {
            int[] finalCoordinates = new int[2];
            switch (i) {
                case 0: activity.findViewById(R.id.bottomPlayerHandLayout).getLocationOnScreen(finalCoordinates); break;
                case 1: activity.findViewById(R.id.leftPlayerHandLayout).getLocationOnScreen(finalCoordinates); break;
                case 2: activity.findViewById(R.id.topPlayerHandLayout).getLocationOnScreen(finalCoordinates); break;
                case 3: activity.findViewById(R.id.rightPlayerHandLayout).getLocationOnScreen(finalCoordinates); break;
            }

            int deltaX = 0, deltaY;
            for(int j = 0; j < manager.players[i].hand.size(); j++) {
                deltaY = (int) (2.5 * (30 - Math.pow(j - manager.getPlayers()[i].hand.size() / 2, 2))); //Truncate the result of the offset
                switch(i) {
                    case 0: finalCoordinates[0] += deltaX;
                        finalCoordinates[1] += 95 - deltaY; break;
                    case 1: finalCoordinates[0] += 100 + deltaY;
                        finalCoordinates[1] += deltaX; break;
                    case 2: finalCoordinates[0] += deltaX;
                        finalCoordinates[1] += 60 + deltaY; break;
                    case 3: finalCoordinates[0] += 115 - deltaY;
                        finalCoordinates[1] += deltaX; break;
                }

                TranslateAnimation ta = new TranslateAnimation(0,finalCoordinates[0]-initialCoordinates[0],0,finalCoordinates[1]-initialCoordinates[1]);
                ta.setDuration(100);
                imageView.startAnimation(ta);

                final int cardsDisplayed = j;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activity.displayIntermediateHands(cardsDisplayed);
                    }
                },100);
            }
        }
    }
}
