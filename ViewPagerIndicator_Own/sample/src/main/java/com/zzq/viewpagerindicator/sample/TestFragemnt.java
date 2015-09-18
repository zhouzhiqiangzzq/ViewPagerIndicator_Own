package com.zzq.viewpagerindicator.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by ie on 2015/9/17.
 */
public class TestFragemnt extends Fragment {

    public static TestFragemnt getInstance(String title) {
        TestFragemnt fragment = new TestFragemnt(title);
        return fragment;
    }

    public TestFragemnt(String title) {
        mTitle = title;
    }


    String mTitle = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment, container, false);
        ((TextView) view.findViewById(R.id.text)).setText(mTitle);
        return view;
    }
}
