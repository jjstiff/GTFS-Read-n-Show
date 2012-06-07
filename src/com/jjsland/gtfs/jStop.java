 package com.jjsland.gtfs;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class jStop extends OverlayItem
{
	int mId;
	ArrayList<jRoute> mRoutes;
	
	public jStop(int id,String name,int latE6,int lonE6)
	{
		super(new GeoPoint(latE6,lonE6),name,Integer.toString(id));
		mId = id;
		mRoutes = new ArrayList<jRoute>(1);
	}
	
	public void addRoute(jRoute route)
	{
		if( route == null ) return;
		if( mRoutes == null ) mRoutes = new ArrayList<jRoute>(1);
		
		jRoute r;
		int a = 0, c = mRoutes.size();
		
		while( a < c )
		{
			int b = (a+c)/2;
			r = mRoutes.get(b);
			if( r == null ) { mRoutes.remove(b); c--; }
			else if( route.mId < r.mId ) c = b;
			else if( route.mId > r.mId ) a = b+1;
			else /* Route is already inserted.. */ return;
		}
		mRoutes.add(a,route);
	}
	
	public ArrayList<jRoute> getRoutes() { return mRoutes; }
	public int getRouteCount() { return mRoutes == null ? 0 : mRoutes.size(); }
	
	public int compareId(jStop t) { return mId-t.mId; }
	public int compareId(int id) { return mId-id; }
	
	public int compareLoc(jStop t)
	{
		return ( mPoint.getLatitudeE6() < t.mPoint.getLatitudeE6() ? -1 
				: ( mPoint.getLatitudeE6() > t.mPoint.getLatitudeE6() ? 1
				: ( mPoint.getLongitudeE6() < t.mPoint.getLongitudeE6() ? -1
				: ( mPoint.getLongitudeE6() > t.mPoint.getLongitudeE6() ? 1
				: 0 ))));
	}
	
	public float getLatF() { return mPoint.getLatitudeE6()/100000.0f; }
	public float getLonF() { return mPoint.getLongitudeE6()/100000.0f; }
	public int getLatE6() { return mPoint.getLatitudeE6(); }
	public int getLonE6() { return mPoint.getLongitudeE6(); }
}
