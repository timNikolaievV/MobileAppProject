package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ViewPagerAdapter
        extends FragmentPagerAdapter {


    public TrainingFragment trainingFragment;
    public PacemakerFragment pacemakerFragment;

    public ViewPagerAdapter(
            @NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        if (position == 0) {
            trainingFragment = new TrainingFragment();
            fragment = trainingFragment;
        } else if (position == 1) {
            pacemakerFragment = new PacemakerFragment();
            fragment = pacemakerFragment;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0)
            title = "Training";
        else if (position == 1)
            title = "Pacemaker";

        return title;
    }
}
