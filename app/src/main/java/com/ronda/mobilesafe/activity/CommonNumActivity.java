package com.ronda.mobilesafe.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ExpandableListView;

import com.ronda.mobilesafe.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

@ContentView(R.layout.activity_common_num)
public class CommonNumActivity extends AppCompatActivity {

    @ViewInject(R.id.elv_common_num)
    private ExpandableListView mElvCommonNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
    }



}
