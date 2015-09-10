package com.example.james.cardsuite;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GameAnimation {
    public static Handler handler = new Handler();

    public static void placeCard(GameActivity activity, View v, final Runnable endAction, int player) {
        int[] potCoordinates = new int[2], cardCoordinates = new int[2];
        v.getLocationOnScreen(cardCoordinates);
        float resetRotation = 0;

        View potCard = activity.findViewById(R.id.anchor);
        potCard.getLocationOnScreen(potCoordinates);

        switch(player) {
            case 0: potCoordinates[0] -= activity.cardWidthPX/2;
                    resetRotation = 0; break;
            case 1: potCoordinates[1] -= activity.cardWidthPX/2;
                resetRotation = 90; break;
            case 2: potCoordinates[0] += activity.cardWidthPX/2;
                resetRotation = 180; break;
            case 3: potCoordinates[1] += activity.cardWidthPX/2;
                resetRotation = 270; break;
        }


        ViewPropertyAnimator animation = v.animate().translationXBy(potCoordinates[0] - cardCoordinates[0]).
                translationYBy(potCoordinates[1]-cardCoordinates[1]).rotation(resetRotation).setDuration(150);

        if(endAction != null)
            animation.withEndAction(endAction);

        animation.start();
    }

    public static void collectEndPile(GameActivity activity, final Runnable endAction, int winningPlayer) {
        int[] pileCoordinates = new int[2];

        View pile = null;
        switch(winningPlayer) {
            case 0: pile = activity.findViewById(R.id.bottomPile); break;
            case 1: pile = activity.findViewById(R.id.leftPile); break;
            case 2: pile = activity.findViewById(R.id.topPile); break;
            case 3: pile = activity.findViewById(R.id.rightPile); break;
        }

        pile.getLocationOnScreen(pileCoordinates);

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

            //Condition (i == 3) is arbitrary; enforce that the Runnable is only run once of the 4 animations.
            if(endAction != null && i == 3) {
                ta.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        handler.post(endAction);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
            }
            v.startAnimation(ta);
        }
    }

    public static AnimatorSet selectSwappedCard(HeartsActivity activity, View v, int currentPlayer) {
        AnimatorSet selectedAnimation = null;
        switch(currentPlayer){
            case 0: selectedAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(activity, R.anim.selected_bottom); break;
            case 1: selectedAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(activity, R.anim.selected_left); break;
            case 2: selectedAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(activity, R.anim.selected_top); break;
            case 3: selectedAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(activity, R.anim.selected_right); break;
        }

        selectedAnimation.setTarget(v);
        selectedAnimation.start();
        return selectedAnimation;
    }

    public static void swapCards(HeartsActivity activity, int swapRound, Runnable endAction, Map<Pair<Integer,View>,AnimatorSet> animations) {
        Set<Pair<Integer,View>> keySet = animations.keySet();
        Iterator<Pair<Integer,View>> iter = keySet.iterator();
        while(iter.hasNext()) {
            Pair<Integer,View> key = iter.next();

            int receivingHand = key.move;
            switch(swapRound) {
                case 0: receivingHand = receivingHand == 0 ? 3 : receivingHand - 1; break;
                case 1: receivingHand = (receivingHand+1)%4; break;
                case 2: receivingHand = (receivingHand+2)%4; break;
            }

            int[] initialCoordinates = new int[2], finalCoordinates = new int[2];
            key.values.getLocationOnScreen(initialCoordinates);

            switch(receivingHand) {
                case 0: activity.findViewById(R.id.bottomPlayerHandLayout).getLocationOnScreen(finalCoordinates);
                    finalCoordinates[0] += activity.findViewById(R.id.bottomPlayerHandLayout).getWidth()/2; break;
                case 1: activity.findViewById(R.id.leftPlayerHandLayout).getLocationOnScreen(finalCoordinates);
                    finalCoordinates[0] += activity.findViewById(R.id.leftPlayerHandLayout).getWidth();
                    finalCoordinates[1] += activity.findViewById(R.id.leftPlayerHandLayout).getHeight()/2; break;
                case 2: activity.findViewById(R.id.topPlayerHandLayout).getLocationOnScreen(finalCoordinates);
                    finalCoordinates[0] += activity.findViewById(R.id.topPlayerHandLayout).getWidth()/2;
                    finalCoordinates[1] += activity.findViewById(R.id.topPlayerHandLayout).getHeight(); break;
                case 3: activity.findViewById(R.id.rightPlayerHandLayout).getLocationOnScreen(finalCoordinates);
                    finalCoordinates[1] += activity.findViewById(R.id.rightPlayerHandLayout).getHeight()/2; break;
            }

            ViewPropertyAnimator animator = key.values.animate().setDuration(150).translationXBy(finalCoordinates[0] - initialCoordinates[0])
                    .translationYBy(finalCoordinates[1] - initialCoordinates[1]);
            animations.get(key).end();
            if(!iter.hasNext() && endAction != null)
                animator.withEndAction(endAction);
            animator.start();
        }
    }

    // Deals one card to each of the 4 players at the same time.
    // If a Runnable is specified, it will run after the last of the 4 card animations (choice is arbitrary).
    public static void dealSingleCards(GameActivity activity, final Runnable endAction, int[] initialCoordinates) {
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

        for(int i = 0; i < 4; i++) {
            final int currentIndex = i;
            final ImageView imageView = new ImageView(activity.getApplicationContext());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

            imageView.setLayoutParams(params);
            imageView.setImageResource(activity.getResources().getIdentifier("cardback", "drawable", activity.getPackageName()));
            imageView.setMaxHeight(150);
            imageView.setAdjustViewBounds(true);
            ((RelativeLayout) activity.findViewById(R.id.potLayout)).addView(imageView);

            imageView.animate().setDuration(75).translationXBy(finalCoordinatesArray[i][0] - initialCoordinates[0])
                    .translationYBy(finalCoordinatesArray[i][1] - initialCoordinates[1])
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            ((ViewGroup) imageView.getParent()).removeView(imageView);

                            //If there isn't a runnable specified or this is the "last of the animations", run the end action.
                            //The choice of index was arbitrary, used only to enforce that 1 of the 4 animations would run the action.
                            if(endAction != null && currentIndex == 3)
                                handler.post(endAction);
                        }
                    }).start();
        }
    }
}
