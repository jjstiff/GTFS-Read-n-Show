package com.jjsland.gtfs;

import android.app.Activity;
import android.os.Bundle;

public class aAboutBox extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }
    @Override
    public void onPause() {
    	super.onPause();
    	finish();
    }
}
