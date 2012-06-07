package com.jjsland.gtfs;

import java.util.Iterator;

import com.google.android.maps.GeoPoint;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

public class aShowMap extends aSuperMap implements GTFSLoadListener, AdapterView.OnItemClickListener
{
	oStopsOverlay mStops;
	
	boolean mOkayToListRoutes;
    laStop mStopLA;

    boolean mZoomedIn = false;
    static final int ZOOM_IN = 16;
    static final int ZOOM_OUT = 11;
	
    @Override
    public void onCreate(Bundle state)
    {
        mOkayToListRoutes = false;
        
        super.onCreate(state);

        // With show map the bundle should contain which data sets to show.
		//Bundle extras = getIntent().getExtras();
		//int route_id = ( extras == null ? 0 : extras.getInt(aShowRoute.ROUTE) );
		//if( route_id > 0 )
		//	mRoute = GTFSLoader.getInstance().getRouteById(route_id);
		//else
		//	mRoute = createTestRoute();
		
		LayoutInflater l = getLayoutInflater();
		View head = l.inflate(R.layout.showmap_head, null);
		View foot = l.inflate(R.layout.showmap_foot, null);
		ViewGroup g = (ViewGroup) findViewById(R.id.superHeadView);
		g.addView(head);
		g = (ViewGroup) findViewById(R.id.superFootView);
		g.addView(foot);
        
        tdStops stopData = mLoader.getStops();
		mStops = new oStopsOverlay(this,stopData);
		addOverlay(mStops);
		
		GeoPoint loc = mLocation.getMyLocation();
		if( loc != null )
		{
			jStop stop = stopData.findClosest(loc.getLatitudeE6(),loc.getLongitudeE6());
			mStops.setHighlight(stop);
			doHighlightStop(stop);
		}
		else
		{
			mStops.setHighlight(null);
			doHighlightStop(null);
		}
		/*
        Context lc = getApplicationContext();
        setContentView(R.layout.showmap);
        MapView mv = (MapView) findViewById(R.id.smMapView);
	    mv.setBuiltInZoomControls(true);
        List<Overlay> mapOverlays = mv.getOverlays();
        mLocation = new MyLocationOverlay(getApplicationContext(),mv);
        mapOverlays.add(mLocation);
        mLoader = GTFSLoader.getInstance();
        mLoader.addGTFSLoadListener(this);
        doHighlightStop(null); */
    }
    @Override
    public void onPause() {
    	super.onPause();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    }
    @Override
    public void onDestroy() {
    	super.onDestroy();
    }
    
    @Override
    protected boolean isRouteDisplayed()
    {
    	return false;
    }
    
    @Override
    public void onGTFSLoadEvent(int step, long time)
    {
    	super.onGTFSLoadEvent(step, time);
    	
    	if( step == GTFSLoader.STEP_STOPS && mLoader != null)
    	{
            mStops = new oStopsOverlay(this,mLoader.getStops());
            addOverlay(mStops);
        }
    	else if( step == GTFSLoader.STEP_TIMES )
    	{
    		enableRouteDisplay();
    	}
    }
    
    protected void enableRouteDisplay()
    {
    	mOkayToListRoutes = true;
    	if( mStops != null )
    		doHighlightStop(mStops.getHighlight());
    }
    
    
    public void doHighlightStop(jStop s)
    {
        TextView tv = (TextView) findViewById(R.id.smListEmpty);
        if( tv != null )
        	tv.setVisibility(mOkayToListRoutes ? View.GONE : View.VISIBLE);
        
        tv = (TextView) findViewById(R.id.smStopName);
    	ListView lv = (ListView) findViewById(R.id.smListRoutes);
    	if( tv == null || lv == null ) return;
    	lv.setVisibility(mOkayToListRoutes ? View.VISIBLE : View.GONE);
    	lv.setOnItemClickListener(this);
    	
    	if( s != null )
    	{
    		tv.setText(s.getTitle());
    		if( mOkayToListRoutes )
    		{
    			if(mStopLA == null) mStopLA = new laStop(s);
    			else mStopLA.setStop(s);
    			
    			lv.setAdapter(mStopLA);
    			//lv.invalidate();
    		}
    	}
    	else
    	{
    		tv.setText("Select a Stop");
    	}
    }
    
    public void showCountStopsInView(int shown, int total)
    {
    	TextView tv = (TextView) findViewById(R.id.smStopCount);
    	if( tv == null ) return;
    	if( shown < total )
    		tv.setText("Showing " + shown + " of " + total + " stops in View.");
    	else
    		tv.setText("Showing " + total + " stops in View.");
    }
    
    public void doSnapMyLocation(View v)
    {
    	snapToMyLocation();
    }
    
    public void doZoomSwitch(View V)
    {
    	if(!mZoomedIn && mStops != null && mStops.mHighlight != null)
    	{
    		snapToStop(mStops.mHighlight);
    		zoomToPreset(ZOOM_IN);
    	}
    	else if(mOkayToListRoutes && mStops != null && mStops.getHighlight() != null)
    	{
    		zoomToRoutes(mStops.getHighlight());
    	}
    	else
    	{
    		zoomToPreset(mZoomedIn ? ZOOM_OUT : ZOOM_IN);
    	}
    	mZoomedIn = !mZoomedIn;
    }
    
    protected void zoomToRoutes(jStop s)
    {
    	if( s == null || s.mRoutes == null ) return;
        
    	Iterator<jRoute> i = s.mRoutes.iterator(); if( !i.hasNext() ) return;
    	jBoundsE6 bounds = new jBoundsE6(i.next().getBounds());
    	while( i.hasNext() )
    		bounds.add(i.next().getBounds());
    	
    	zoomToBounds(bounds);
    }
    
	@Override
	public void onItemClick(AdapterView<?> a, View v, int p, long id)
	{
		jRoute r = (jRoute) a.getAdapter().getItem(p);
	    Intent i = new Intent(this,aShowRoute.class);
	    i.putExtra(aShowRoute.ROUTE, r.mId);
	    startActivity(i);
	}
}
