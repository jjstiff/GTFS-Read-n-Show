package com.jjsland.gtfs;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class aShowStops extends ListActivity implements GTFSLoadListener {

	GTFSLoader mLoader;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoader = gtfsApp.getFirstLoader();
        mLoader.addGTFSLoadListener(this);
    }
    @Override
    public void onDestroy() {
    	super.onDestroy();
        mLoader.removeGTFSLoadListener(this);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	// Do nothing
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int p, long id)
	{
		jStop s = (jStop) l.getAdapter().getItem(p);
		Toast t = Toast.makeText(getApplicationContext(),s.getTitle(),Toast.LENGTH_SHORT);
		t.show();
	}
    
	public void onGTFSLoadEvent(int step, long time)
	{
		switch( step )
		{
		case GTFSLoader.STEP_STOPS:
			setListAdapter(mLoader.getStops());
			break;
		case GTFSLoader.STEP_TRIPS:
			setListAdapter(mLoader.getStops());
			break;
		case GTFSLoader.STEP_TIMES:
			setListAdapter(mLoader.getStops());
			break;
		}
	}
    public void onGTFSLoadStopTimeUpdate(int count, long bytesRead, long bytesTotal ) {}
}
