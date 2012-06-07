package com.jjsland.gtfs;

import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class oRouteOverlay extends Overlay {
	
	jRoute mRoute;
	jStop mHighlight;
	MapView mMapView;
	aShowRoute mActivity;
	
	public oRouteOverlay(aShowRoute pActivity, MapView pView)
	{
		super();
		mRoute = null;
		mHighlight = null;
		mActivity = pActivity;
		mMapView = pView;
	}
	
	public void setRoute(jRoute r) { mRoute = r; }
	
	float mLastX, mLastY;
	final static float TOUCH_RADIUS_SQUARED = 15.0f*15.0f;
	@Override
	public boolean onTouchEvent(MotionEvent e, MapView v)
	{ 
		float x = e.getX();
		float y = e.getY();
		if( e.getAction() == MotionEvent.ACTION_DOWN )
		{
			mLastX = x; mLastY = y;
			return false;
		}
		if( e.getAction() == MotionEvent.ACTION_UP &&
			((x-mLastX)*(x-mLastX) + (y-mLastY)*(y-mLastY)) <= TOUCH_RADIUS_SQUARED )
		{
			Projection p = v.getProjection();
			GeoPoint gp = p.fromPixels((int)x,(int)y);
			int i = mRoute.findClosestStopIndex(gp.getLatitudeE6(),gp.getLongitudeE6());
			setHighlight(mRoute.getStopAtIndex(i));
			notifyListView(i,mHighlight);
			return true;
		} 
		return false;
	}
	
	void notifyListView(int p,jStop s)
	{
		if( mActivity != null )
			mActivity.setHighlight(p,s);
	}
	void setHighlight(jStop s)
	{
		mHighlight = s;
		if( mMapView != null )
			mMapView.invalidate();
	}

	static Paint COLOR_STOP = new Paint();
	static Paint COLOR_HILIGHT = new Paint();
	static Paint COLOR_SHADOW = new Paint();
	static Paint COLOR_OUTLINE = new Paint();
	static {
		COLOR_STOP.setARGB(255,255,125,125); COLOR_STOP.setStrokeWidth(3);
		COLOR_HILIGHT.setARGB(255,125,125,255); COLOR_HILIGHT.setStrokeWidth(3);
		COLOR_SHADOW.setARGB(255,200,200,200); COLOR_SHADOW.setStrokeWidth(3);
		COLOR_OUTLINE.setARGB(255,50,50,100); COLOR_OUTLINE.setStrokeWidth(3);
			COLOR_OUTLINE.setStyle(Paint.Style.STROKE);
	}
	
	public void draw(Canvas c, MapView mv, boolean shadow)
	{
		if( mRoute == null ) return;
		
		jTripSeq jts = mRoute.getSelectedSeq();
		if( jts == null ) return;
		jTrip jt = jts.getSelectedTrip();
		if( jt == null ) return;
		Iterator<jStop> j = jt.mSeq.mStops.iterator();
		
		jStop t;
		int x=0, y=0;
		boolean first = true;
		Projection p = mv.getProjection();
		Point l = new Point();
		int zoom = mv.getZoomLevel();
		
		while( j.hasNext() )
		{
			t = j.next();
			if( t == null ) continue;
			p.toPixels(t.getPoint(),l);
			if( !first && shadow )
				c.drawLine(x,y,l.x,l.y,COLOR_HILIGHT);
			first = false;
			x = l.x; y = l.y;
			c.drawCircle(l.x,l.y,radius(t.mRoutes.size(),zoom),shadow?COLOR_HILIGHT:(t==mHighlight?COLOR_STOP:COLOR_OUTLINE));
		}
	}
	
	static void drawRouteLines(jStop s, Canvas c, Projection p, Point l, int zoom)
	{ /*
		jRoute r;
		if(s.mRoutes == null) return;
		Iterator<jRoute> i = s.mRoutes.iterator();
		while( i.hasNext() )
		{
			r = i.next();
			if( r.mDir1 == null || r.mDir2 == null ) continue;
			
			if( r.mDir1.contains(s) )
			{
				drawRouteLinesWithDir(s,c,p,l,zoom,r.mDir2);
				drawRouteLinesWithDir(s,c,p,l,zoom,r.mDir1);
			}
			else
			{
				drawRouteLinesWithDir(s,c,p,l,zoom,r.mDir1);
				drawRouteLinesWithDir(s,c,p,l,zoom,r.mDir2);
			}
		} */
	}
	
	static void drawRouteLinesWithDir( jStop s, Canvas c, Projection p, Point l, int zoom, ArrayList<jStop> dir )
	{
		Iterator<jStop> j = dir.iterator();
		boolean found = false;
		jStop t;
		int x=0, y=0;
		boolean first = true;
		
		while( j.hasNext() )
		{
			t = j.next();
			if( t == null ) continue;
			p.toPixels(t.getPoint(),l);
			if( !first )
				c.drawLine(x,y,l.x,l.y,found?COLOR_HILIGHT:COLOR_STOP);
			first = false;
			x = l.x; y = l.y;
			if( t == s ) { found = true; }
			if( found )
				c.drawCircle(l.x,l.y,radius(s.mRoutes.size(),zoom),COLOR_HILIGHT);
		}
	}
	
	static int radius(int routeCount,int zoomLevel)
	{
		return 2 + zoomLevel/4;
	}

}
