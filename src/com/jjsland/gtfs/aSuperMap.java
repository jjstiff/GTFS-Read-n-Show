package com.jjsland.gtfs;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

abstract public class aSuperMap extends MapActivity implements GTFSLoadListener
{
	MyLocationOverlay mLocation;
	GTFSLoader mLoader;
	oBoundsOverlay mBounds;
	static final int MAP_VIEW = R.id.superMapView;
	static final int MAP_LAYOUT = R.layout.supermap;
	
	@Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        Context c = getApplicationContext();
        setContentView(MAP_LAYOUT);
        MapView mv = (MapView) findViewById(MAP_VIEW);
	    mv.setBuiltInZoomControls(true);
        List<Overlay> mapOverlays = mv.getOverlays();
        mLocation = new MyLocationOverlay(c,mv);
        mapOverlays.add(mLocation);
        mLoader = gtfsApp.getFirstLoader();
        mLoader.addGTFSLoadListener(this);
    }
    @Override
    public void onPause() {
    	super.onPause();
        mLocation.disableCompass();
        mLocation.disableMyLocation();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        mLocation.enableCompass();
        mLocation.enableMyLocation();
    }
    @Override
    public void onDestroy() {
    	super.onDestroy();
        mLoader.removeGTFSLoadListener(this);
    }
    
    public void addOverlay(Overlay o)
    {
        MapView mv = (MapView) findViewById(MAP_VIEW); if( mv == null ) return;
        List<Overlay> overlays = mv.getOverlays(); if( overlays == null ) return;
        overlays.add(o);
    }
    
    public MapView getMapView()
    {
    	return 	(MapView) findViewById(MAP_VIEW);
		
    }
    
    public void onGTFSLoadEvent(int step, long time)
    {
    	if( step == GTFSLoader.STEP_STOPS && mLoader != null)
    	{
    		jBoundsE6 bounds = mLoader.getBounds();
    		mBounds = new oBoundsOverlay(bounds);
    		addOverlay(mBounds);
    		zoomToBounds(bounds);
    	}
    }
    public void onGTFSLoadStopTimeUpdate(int count, long bytesRead, long bytesTotal) {}
    
    public void snapToMyLocation()
    {
    	if(mLocation == null) return;
    	GeoPoint l = mLocation.getMyLocation(); if( l == null ) return;
        MapView mv = (MapView) findViewById(MAP_VIEW); if( mv == null ) return;
    	MapController mc = mv.getController(); if( mc == null ) return;
    	mc.animateTo(l);
    }
    
    protected void snapToStop(jStop s)
    {
    	if( s == null ) return;
    	
        MapView mv = (MapView) findViewById(MAP_VIEW); if( mv == null ) return;
        MapController mc = mv.getController();  if( mc == null ) return;
        mc.setCenter(s.getPoint());
    }
    
    protected void zoomToRoute(jRoute r)
    {
		if( r == null ) return;
		zoomToBounds(r.getBounds());
    }
    
    protected void zoomToBounds(jBoundsE6 bounds)
    {
    	if( bounds == null ) return;
    	MapView mv = (MapView) findViewById(MAP_VIEW); if( mv == null ) return;
        MapController mc = mv.getController(); if( mc == null ) return;
        
        mc.setCenter(bounds.getCenter());
        mc.zoomToSpan(bounds.getLatSpanE6(),bounds.getLonSpanE6());
    }
    
    protected void zoomToPreset(int z)
    {
    	MapView mv = (MapView) findViewById(MAP_VIEW); if( mv == null ) return;
        MapController mc = mv.getController(); if( mc == null ) return;
    	mc.setZoom(z);
    }
    
    protected void refreshMap()
    {
    	MapView mv = (MapView) findViewById(MAP_VIEW); if( mv == null ) return;
    	mv.invalidate();
    }

    void refreshDayButtons()
    {
		Button b = ((Button)findViewById(R.id.bDayAll));
		if( b != null ) b.setEnabled(!jCalendar.isShowing_all());
		b = ((Button)findViewById(R.id.bDaySUN));
		if( b != null ) b.setEnabled(!jCalendar.isShowing_sun_only());
		b = ((Button)findViewById(R.id.bDaySAT));
		if( b != null ) b.setEnabled(!jCalendar.isShowing_sat_only());
		b = ((Button)findViewById(R.id.bDayMTWRF));
		if( b != null ) b.setEnabled(!jCalendar.isShowing_mtwrf_only());
		b = ((Button)findViewById(R.id.bDayToday));
		if( b != null ) b.setEnabled(!jCalendar.isShowing_today() || jCalendar.isShowing_all());
		b = ((Button)findViewById(R.id.bDayTomorrow));
		if( b != null ) b.setEnabled(!jCalendar.isShowing_tomorrow() || jCalendar.isShowing_all());
    }
    
    public void doDaySUN(View v)
    {
    	jCalendar.setWhenToShow(jCalendar.SUN);
    	refreshDayButtons();
    }
    public void doDaySAT(View v)
    {
    	jCalendar.setWhenToShow(jCalendar.SAT);
    	refreshDayButtons();
    }
    public void doDayMTWRF(View v)
    {
    	int w = jCalendar.MON;
    	w |= jCalendar.TUE;
    	w |= jCalendar.WED;
    	w |= jCalendar.THU;
    	w |= jCalendar.FRI;
    	jCalendar.setWhenToShow(w);
    	refreshDayButtons();
    }
    public void doDayAll(View v)
    {
    	int w = jCalendar.SUN;
    	w |= jCalendar.MON;
    	w |= jCalendar.TUE;
    	w |= jCalendar.WED;
    	w |= jCalendar.THU;
    	w |= jCalendar.FRI;
    	w |= jCalendar.SAT;
    	jCalendar.setWhenToShow(w);
    	refreshDayButtons();
    }
    public void doDayToday(View v)
    {
    	jCalendar.setWhenToShow(jCalendar.TODAY);
    	refreshDayButtons();
    }
    public void doDayTomorrow(View v)
    {
    	jCalendar.setWhenToShow(jCalendar.TOMORROW);
    	refreshDayButtons();
    }

}
