package com.gtjgroup.cardsuite;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class PageViewActivity extends FragmentActivity {

    private static final int NUM_PAGES = 5;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private int gameMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_layout);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    public void bridgeHelp(View v) {
        if (gameMode != 0) {
            gameMode = 0;
            resetBorders(v);
            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            mPager.setAdapter(mPagerAdapter);
        }
    }

    public void heartsHelp(View v) {
        if (gameMode != 1) {
            gameMode = 1;
            resetBorders(v);
            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            mPager.setAdapter(mPagerAdapter);
        }
    }

    public void spadesHelp(View v) {
        if (gameMode != 2) {
            gameMode = 2;
            resetBorders(v);
            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            mPager.setAdapter(mPagerAdapter);
        }
    }

    public void resetBorders(View v) {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.help_layout);
        for(int i = 1; i < 4; i++) {
            if (i - 1 != gameMode) {
                ImageButton button = (ImageButton) relativeLayout.getChildAt(i);
                button.setBackgroundResource(R.drawable.help_border_unselected);
            }
        }
        v.setBackgroundResource(R.drawable.help_button_background);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(View collection, int position, Object o) {
            View view = (View)o;
            ((ViewPager) collection).removeView(view);
            view = null;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return PageViewerFragment.newInstance(1, "Page # 1", gameMode);
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return PageViewerFragment.newInstance(2, "Page # 2", gameMode);
                case 2:
                    return PageViewerFragment.newInstance(3, "Page # 3", gameMode);
                case 3:
                    return PageViewerFragment.newInstance(4, "Page # 4", gameMode);
                case 4:
                    return PageViewerFragment.newInstance(5, "Page # 5", gameMode);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

    }
}