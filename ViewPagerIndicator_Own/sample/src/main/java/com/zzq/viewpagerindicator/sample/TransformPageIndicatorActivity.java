package com.zzq.viewpagerindicator.sample;


import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.zzq.viewpagerindicator.TransformPageIndicator;

/**
 * Created by ie on 2015/9/17.
 */
public class TransformPageIndicatorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_tfp_indicator);

        TransformPageIndicator TransformPageIndicator = (com.zzq.viewpagerindicator.TransformPageIndicator) findViewById(R.id.main_page_indicator);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.main_viewpager);
        mViewPager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
        TransformPageIndicator.setViewPager(mViewPager);
    }
}
