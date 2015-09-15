package com.example.james.cardsuite;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PageViewerFragment extends Fragment {

    private String title;
    private int page;
    private int gameMode;

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
        if (page < 6) {
            rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_page_viewer, container, false);
            TextView text = (TextView) rootView.findViewById(R.id.help_text);
            TextView titleText = (TextView) rootView.findViewById(R.id.help_title);
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
                case 1: titleText.setText(getResources().getString(R.string.help_title1)); break;
                case 2: titleText.setText(getResources().getString(R.string.help_title2)); break;
                case 3: titleText.setText(getResources().getString(R.string.help_title3)); break;
                case 4: titleText.setText(getResources().getString(R.string.help_title4)); break;
                case 5: titleText.setText(getResources().getString(R.string.help_title5)); break;
            }
        }

        return rootView;
    }
}