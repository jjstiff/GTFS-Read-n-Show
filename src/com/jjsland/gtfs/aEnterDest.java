package com.jjsland.gtfs;

import java.io.IOException;
import java.util.List;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Allows you to add a Destination to the list of user-defined destinations.
 * 
 * @author JJ Stiff
 *
 */
public class aEnterDest extends MapActivity
	implements GTFSLoadListener, OnItemClickListener
{
	Geocoder mGeo;
	GTFSLoader mLoader;
	oDestResults mResults;
	MyLocationOverlay mLocation;
	oBoundsOverlay mBounds;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Context c = getApplicationContext();
        
        mGeo = new Geocoder(c);
        mLoader = gtfsApp.getFirstLoader();
        mResults = new oDestResults();
        
        setContentView(R.layout.enterdest);
        MapView mv = (MapView) findViewById(R.id.edMapView);
	    mv.setBuiltInZoomControls(true);
	    
        mLocation = new MyLocationOverlay(c,mv);
	    
        List<Overlay> mapOverlays = mv.getOverlays();
        mapOverlays.add(mResults);
        mapOverlays.add(mLocation);
        
    	ListView lv = (ListView) findViewById(R.id.edResults);
    	lv.setOnItemClickListener(this);
    	
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
        mLocation.disableCompass();
        mLocation.disableMyLocation();
        
        // TODO: Save Search Results.
    }
    
    @Override
    public void onResume() {
    	super.onResume();
        mLocation.enableCompass();
        mLocation.enableMyLocation();
    }
    
    public boolean isRouteDisplayed() { return false; }
    
    public void onGTFSLoadEvent(int step, long time)
    {
    	if( step == GTFSLoader.STEP_STOPS && mLoader != null )
    	{
    		jBoundsE6 bounds = mLoader.getBounds();
    		if( bounds != null )
    		{
    			mBounds = new oBoundsOverlay(bounds,1.1f);
    		
    			MapView mv = (MapView) findViewById(R.id.edMapView);
    			if( mv != null )
    			{
    				List<Overlay> l = mv.getOverlays();
    				if( l != null ) l.add(mBounds);
    			}
    			zoomToBounds();
    		}
    	}
    }
    public void onGTFSLoadStopTimeUpdate(int count, long bytesRead, long bytesTotal) {}
    
    public void zoomToBounds()
    {
    	if(mBounds == null) return;
		MapView mv = (MapView) findViewById(R.id.edMapView); if( mv == null ) return;
		MapController mc = mv.getController(); if( mc == null ) return;
        mc.setCenter(mBounds.getCenterPoint());
        mc.zoomToSpan(mBounds.getLatSpanE6(true),mBounds.getLonSpanE6(true));
    }
    
    public void doFindDest(View v)
    {
    	EditText et = (EditText) findViewById(R.id.edText);
    	ListView lv = (ListView) findViewById(R.id.edResults);
    	CharSequence cs = et.getText();
    	if( mGeo != null ) {
    		try {
    	    	  
    			List<Address> addr;
    			if( mBounds != null )
    				addr = mGeo.getFromLocationName(cs.toString(),5,
    						mBounds.mBounds.getLowLatE6(),mBounds.mBounds.getLowLonE6(),
    						mBounds.mBounds.getHighLatE6(),mBounds.mBounds.getHighLonE6());
    			else
    				addr = mGeo.getFromLocationName(cs.toString(),5);
    			mResults.setData(addr);
    			lv.setAdapter(mResults);
    		}
    		catch(IOException e){
    			lv.setAdapter(new ArrayAdapter<String>(this,R.layout.ed_row,R.id.label,new String[] {"No access to internet."}));
    		}
    	}
    }
    
    public void onItemClick(AdapterView<?> parent, View view, int p, long id)
    {
    	Address a = mResults.mData.get(p);
    	Toast t = Toast.makeText(getApplicationContext(),a.getAddressLine(0)+"\n"+ a.getAddressLine(1)+"\nNot Yet Impelemented...",Toast.LENGTH_SHORT);
		t.show();
    }
 
}
