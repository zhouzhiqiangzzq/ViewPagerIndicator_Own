package com.zzq.viewpagerindicator.sample;


import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.zzq.viewpagerindicator.TitlePageIndicator;

/**
 * Created by ie on 2015/9/17.
 */
public class TitlePageIndicatorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiity_tp_indicator);

        TitlePageIndicator TitlePageIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new FragmentAdapter2(getSupportFragmentManager()));
        TitlePageIndicator.setViewPager(mViewPager);
    }
}
