package com.example.hw;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

public class BottomBarAdapter extends SmartFragmentStatePagerAdapter {
    private static final String LOG_TAG = BottomBarAdapter.class.getSimpleName();
    private final List<Fragment> fragments = new ArrayList<>();

    public BottomBarAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }
    // Our custom method that populates this Adapter with Fragments
    public void addFragments(Fragment fragment) {
        fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemPosition(Object object){
        int index = fragments.indexOf(object);
        if (index == -1) {
            return POSITION_NONE;
        }
        else {
            return index;
        }
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}