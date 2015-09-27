package com.gtjgroup.cardsuite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class PageViewerFragment extends Fragment {

    private String title;
    private int page;
    private int gameMode;
    boolean rightMomentum = true;

    // newInstance constructor for creating fragment with arguments
    public static PageViewerFragment newInstance(int page, String title, int gameMode) {
        PageViewerFragment fragmentFirst = new PageViewerFragment();
        Bundle args = new Bundle();
        args.putInt("pageNum", page);
        args.putString("title", title);
        args.putInt("gameMode", gameMode);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("pageNum", 0);
        title = getArguments().getString("title");
        gameMode = getArguments().getInt("gameMode", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = null;
        if (page == 1) {

        }
        if (page < 6) {
            rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_page_viewer, container, false);
            TextView text = (TextView) rootView.findViewById(R.id.help_text);
            TextView titleText = (TextView) rootView.findViewById(R.id.help_title);
            ImageButton left = (ImageButton) rootView.findViewById(R.id.leftHelp);
            ImageButton right = (ImageButton) rootView.findViewById(R.id.rightHelp);
            left.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setAlpha(0.5f);
                        leftClick(v);
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        v.setAlpha(1f);
                        return true;
                    }

                    return false;
                }
            });
            right.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setAlpha(0.5f);
                        rightClick(v);
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        v.setAlpha(1f);
                        return true;
                    }

                    return false;
                }
            });
            if (gameMode == 0) {
                switch (page) {
                    case 1: text.setText(getResources().getString(R.string.bridge1)); break;
                    case 2: text.setText(getResources().getString(R.string.bridge2)); break;
                    case 3: text.setText(getResources().getString(R.string.bridge3)); break;
                    case 4: text.setText(getResources().getString(R.string.bridge4)); break;
                    case 5: text.setText(getResources().getString(R.string.bridge5)); break;
                }
            }
            else if (gameMode == 1)
                switch (page) {
                    case 1: text.setText(getResources().getString(R.string.hearts1)); break;
                    case 2: text.setText(getResources().getString(R.string.hearts2)); break;
                    case 3: text.setText(getResources().getString(R.string.hearts3)); break;
                    case 4: text.setText(getResources().getString(R.string.hearts4)); break;
                    case 5: text.setText(getResources().getString(R.string.hearts5)); break;
                }
            else {
                switch (page) {
                    case 1: text.setText(getResources().getString(R.string.spades1)); break;
                    case 2: text.setText(getResources().getString(R.string.spades2)); break;
                    case 3: text.setText(getResources().getString(R.string.spades3)); break;
                    case 4: text.setText(getResources().getString(R.string.spades4)); break;
                    case 5: text.setText(getResources().getString(R.string.spades5)); break;
                }
            }
            switch (page) {
                case 1:
                    titleText.setText(getResources().getString(R.string.help_title1));
                    right.setVisibility(View.VISIBLE);
                    left.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    titleText.setText(getResources().getString(R.string.help_title2));
                    right.setVisibility(View.VISIBLE);
                    left.setVisibility(View.VISIBLE);
                    break;
                case 3: titleText.setText(getResources().getString(R.string.help_title3));
                    right.setVisibility(View.VISIBLE);
                    left.setVisibility(View.VISIBLE);
                    break;
                case 4:
                    titleText.setText(getResources().getString(R.string.help_title4));
                    right.setVisibility(View.VISIBLE);
                    left.setVisibility(View.VISIBLE);
                    break;
                case 5:
                    titleText.setText(getResources().getString(R.string.help_title5));
                    right.setVisibility(View.INVISIBLE);
                    left.setVisibility(View.VISIBLE);
                    break;
            }

            TextView menuText = (TextView) rootView.findViewById(R.id.menu_from_help);
            menuText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        v.setAlpha(0.5f);
                        helpToMenu(v);
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        v.setAlpha(1f);
                        return true;
                    }

                    return false;
                }
            });
        }

        return rootView;
    }

    public void leftClick(View v) {
        ViewPager vp=(ViewPager) getActivity().findViewById(R.id.pager);
        if (rightMomentum) {
            page -= 2;
            rightMomentum = false;
        }
        vp.setCurrentItem(page, true);
    }

    public void rightClick(View v) {
        ViewPager vp=(ViewPager) getActivity().findViewById(R.id.pager);
        if (!rightMomentum) {
            page += 2;
            rightMomentum = true;
        }
        vp.setCurrentItem(page, true);
    }

    public void helpToMenu(View v) {
        startActivity(new Intent(this.getContext(), MainActivity.class));
    }
}