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

public class oStopsOverlay extends Overlay
{
	tdStops mStops;
	aShowMap mAct;
	
	public oStopsOverlay(aShowMap activity, tdStops stops)
	{
		super();
		mStops = stops;
		mAct = activity;
	}
	
	jStop mHighlight;
	
	float mLastX = -1;
	float mLastY = -1;
	static final float TOUCH_RADIUS_SQUARED = 225;
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
		if( e.getAction() != MotionEvent.ACTION_UP ||
			((x-mLastX)*(x-mLastX) + (y-mLastY)*(y-mLastY))
			 > TOUCH_RADIUS_SQUARED )
			return false;
		
		Projection p = v.getProjection();
		GeoPoint gp = p.fromPixels((int)x,(int)y);
		setHighlight(mStops.findClosest(gp.getLatitudeE6(),gp.getLongitudeE6()));
		
		mAct.doHighlightStop(mHighlight);
		
		return true;
	}
	
	public void setHighlight( jStop h ) { mHighlight = h; }
	public jStop getHighlight() { return mHighlight; }
	

	static Paint COLOR_STOP = new Paint();
	static Paint COLOR_HILIGHT = new Paint();
	static Paint COLOR_SHADOW = new Paint();
	static {
		COLOR_STOP.setARGB(255,255,125,125); COLOR_STOP.setStrokeWidth(3);
		COLOR_HILIGHT.setARGB(255,125,125,255); COLOR_HILIGHT.setStrokeWidth(3);
		COLOR_SHADOW.setARGB(255,200,200,200); COLOR_SHADOW.setStrokeWidth(3);
	}
	
	public void draw(Canvas c, MapView mv, boolean shadow)
	{
		Projection p = mv.getProjection();
		GeoPoint gp = mv.getMapCenter();
		int lat = mv.getLatitudeSpan();
		int lon = mv.getLongitudeSpan();
		ArrayList<jStop> stops = mStops.getStopsInView(gp,lat,lon);
		
		int size = stops.size(), i, r;
		int zoom = mv.getZoomLevel();
		int step = 1 + (size/120);
		Point l = new Point();
		jStop s;
		if( shadow )
		{
			for( i = 0 ; i < size; i += step )
			{
				s = stops.get(i);
				p.toPixels(s.getPoint(),l); 
				r = radius(s.getRouteCount(),zoom);
				c.drawCircle(l.x+1,l.y+1,r+2,COLOR_SHADOW);
			}
			if( mHighlight != null )
			{
				p.toPixels(mHighlight.getPoint(),l); 
				r = radius(mHighlight.getRouteCount(),zoom);
				c.drawCircle(l.x+1,l.y+1,r+2,COLOR_SHADOW);
			}
		}
		else
		{
			int disp_count = 0;
			for( i = 0 ; i < size; i += step )
			{
				s = stops.get(i);
				p.toPixels(s.getPoint(),l); 
				r = radius(s.mRoutes.size(),zoom);
				c.drawCircle(l.x,l.y,r,COLOR_STOP);
				disp_count++;
			}
			if( mHighlight != null )
			{
				p.toPixels(mHighlight.getPoint(),l); 
				r = radius(mHighlight.getRouteCount(),zoom);
				c.drawCircle(l.x,l.y,r,COLOR_HILIGHT);
				
				drawRouteLines(mHighlight,c,p,l,zoom);
			}
			mAct.showCountStopsInView(disp_count,size);
		}
		//mv.setBuiltInZoomControls(true);
	}
	
	static void drawRouteLines(jStop s, Canvas c, Projection p, Point reuse, int zoom)
	{
		if(s == null || s.getRouteCount() <= 0 ) return;
	
		Iterator<jRoute> i = s.getRoutes().iterator();
		while( i.hasNext() )
		{
			jRoute r = i.next(); if( r == null || r.getSequences() == null ) continue;
			Iterator<jTripSeq> j = r.getSequences().iterator();
			while( j.hasNext() )
			{
				jTripSeq jts = j.next();
				if( jts != null ) jts.drawSeqLines(s,c,p,reuse,zoom);
			}
		}
	}
	/*
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
	}*/
	static int radius(int routeCount,int zoomLevel)
	{
		return 2 + zoomLevel/4;
	}
}
