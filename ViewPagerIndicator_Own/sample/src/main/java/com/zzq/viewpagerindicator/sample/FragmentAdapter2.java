package com.zzq.viewpagerindicator.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by ie on 2015/9/17.
 */
public class FragmentAdapter2 extends FragmentPagerAdapter {

    public FragmentAdapter2(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        return TestFragemnt.getInstance(getPageTitle(position).toString());
    }

    @Override
    public int getCount() {
        return Common.STR2.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Common.STR2[position];
    }


}
