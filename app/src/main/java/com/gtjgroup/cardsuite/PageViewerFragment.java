package com.gtjgroup.cardsuite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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

    public ViewGroup createTextPage(LayoutInflater inflater, ViewGroup container) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_page_viewer, container, false);
        return rootView;
    }

    public ViewGroup createPicPage(LayoutInflater inflater, ViewGroup container) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_page_pic_text, container, false);
        ImageView helpImage = (ImageView) rootView.findViewById(R.id.help_pic);
        helpImage.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
        helpImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        return rootView;
    }

    public ViewGroup bridgeInstructions(LayoutInflater inflater, ViewGroup container) {
        ViewGroup rootView = null;
        switch (page) {
            case 1: rootView = createTextPage(inflater, container); break;
            case 2: rootView = createPicPage(inflater, container); break;
        }
        switch (page) {
            case 2:
                ImageView helpImage = (ImageView) rootView.findViewById(R.id.help_pic);
                helpImage.setImageResource(R.drawable.board); break;
            case 3:
        }
        return rootView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = bridgeInstructions(inflater, container);
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