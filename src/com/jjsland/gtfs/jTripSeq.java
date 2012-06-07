package com.jjsland.gtfs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import android.graphics.Canvas;
import android.graphics.Point;

import com.google.android.maps.Projection;

public class jTripSeq {
	
	String mSeqName;
	ArrayList<jTrip> mTrips;
	
	public jTripSeq(String name)
	{
		mSeqName = name;
		mTrips = new ArrayList<jTrip>(10);
		mSelectedTrip = 0;
	}
	
	void addTripById(jTrip t)
	{
		int a = 0, c = mTrips.size()-1;
		if( c < 0 ) { mTrips.add(t); return; }
		if( t.compareId(mTrips.get(c)) > 0 ) { mTrips.add(c+1,t); return; }
		if( t.compareId(mTrips.get(a)) < 0 ) { mTrips.add(a,t); return; }
	
		while( a < c )
		{
			int b = (a+c)/2;
			int v = t.compareId(mTrips.get(b));
			if( v == 0 ) return; // Trip already added.
			if( v < 0 ) { c = b; }
			else { a = b+1; }
		}
		mTrips.add(c,t);
		return;
	}
	
	void trimToSize() { mTrips.trimToSize(); }
	
	/**
	 * For each trip in each tripseq, normalize the tripseqdata to make tripseqdata.mtimes[0] = 0
	 */
	void normalize()
	{
		Iterator<jTrip> i = mTrips.iterator();
		while(i.hasNext())
		{
			jTrip t = i.next();
			if( t != null)
				t.normalize();
		}
	}
	
	ArrayList<jTripSeq> combineDuplicateSequences()
	{
		if( mTrips == null || mTrips.size() <= 1 ) return null;
		jTrip cmp = mTrips.get(0);
		jTripSeq extra = null;
		for( int i = 1; i < mTrips.size(); i++ )
		{
			jTrip t = mTrips.get(i);
			if( !cmp.matchesSeq(t) )
			{
				if( extra == null )
					extra = new jTripSeq(mSeqName+" x");
				extra.addTripById(t);
				mTrips.remove(i);
				i--;
			}
		}
		if( extra == null ) return null;
		ArrayList<jTripSeq> out = extra.combineDuplicateSequences();
		if( out == null )
			out = new ArrayList<jTripSeq>();
		out.add(extra);
		return out;
	}
	
	/**
	 * Used for display.
	 */
	int mSelectedTrip;
	/**
	 * Increments to the next trip visible (jCalendar.showTrip).
	 * Does not change if there is no next trip visible.
	 */
	public void selectNextTrip()
	{
		int s = mSelectedTrip+1;
		if( s < 0 ) s = 0;
		
		for( ; s < mTrips.size(); s++ )
		{
			if( jCalendar.showTrip(mTrips.get(s)) )
			{
				mSelectedTrip = s;
				break;
			}
		}
	}
	public boolean hasNextTrip()
	{
		int m = mSelectedTrip;
		selectNextTrip();
		if( m != mSelectedTrip )
		{
			mSelectedTrip = m;
			return true;
		}
		else return false;
	}
	
	public boolean containsStop(jStop s)
	{
		Iterator<jTrip> i = mTrips.iterator();
		while( i.hasNext() )
		{
			jTrip t = i.next();
			if( t != null )
			{
				int index = t.getStopIndex(s);
				if( index >= 0 ) return true;
			}
		}
		return false;
	}
	
	public String getNext3TimesString(jStop s)
	{
		selectTripForNow(s);
		jTrip t = getSelectedTrip();
		if( t == null ) return "No Trips.";
		int index = t.getStopIndex(s);
		String out = t.getTimeString(index);
		
		t = getNextTrip();
		if( t == null ) return out;
		out += ", " + t.getTimeString(index);
		
		t = getNextTrip2();
		if( t == null ) return out;
		out += ", " + t.getTimeString(index);
		
		return out;
	}
	
	/**
	 * Decrements to the next trip visible (jCalendar.showTrip).
	 * Does not change if there is no next trip visible.
	 */
	public void selectPrevTrip()
	{
		int s = mSelectedTrip-1;
		if( s >= mTrips.size() ) s = mTrips.size()-1;
		
		for( ; s >= 0; s-- )
		{
			if( jCalendar.showTrip(mTrips.get(s)) )
			{
				mSelectedTrip = s;
				break;
			}
		}
	}
	public boolean hasPrevTrip()
	{
		int m = mSelectedTrip;
		selectPrevTrip();
		if( m != mSelectedTrip )
		{
			mSelectedTrip = m;
			return true;
		}
		else return false;
	}
	
	public void selectTripForNow(jStop stop)
	{
		Calendar c = Calendar.getInstance();
		int seconds_in_day
				= c.get(Calendar.HOUR_OF_DAY)*60*60
				+ c.get(Calendar.MINUTE) * 60
				+ c.get(Calendar.SECOND);
		int i;
		for( i = 0; i < mTrips.size(); i++ )
		{
			jTrip t = mTrips.get(i); if( t == null ) continue;
			if( t.getStopTime_seconds(stop)-seconds_in_day > 0 ) break;
		}
		if( i >= mTrips.size() ) return;
		if( i > 0 ) i--;
		mSelectedTrip = i;		
	}
	
	/**
	 * On some days the bus might not run.
	 * If the bus is not running on the jCalendar selected day, this returns null.
	 * @return
	 */
	public jTrip getSelectedTrip()
	{
		jTrip out;
		if( mSelectedTrip >= 0 && mSelectedTrip < mTrips.size() )
		{
			out = mTrips.get(mSelectedTrip);
			if( jCalendar.showTrip(out) )
				return out;
		}
		int m = mSelectedTrip;
		mSelectedTrip = -1;
		selectNextTrip();
		if( mSelectedTrip >= 0 && mSelectedTrip < mTrips.size() )
			return mTrips.get(mSelectedTrip);
		
		mSelectedTrip = m;
		return null;
	}
	
	public jTrip getNextTrip()
	{
		int m = mSelectedTrip;
		selectNextTrip();
		if( m == mSelectedTrip ) return null;
		jTrip out = getSelectedTrip();
		mSelectedTrip = m;
		return out;
	}
	
	public jTrip getNextTrip2()
	{
		int m = mSelectedTrip;
		selectNextTrip();
		if( m == mSelectedTrip ) return null;
		int n = mSelectedTrip;
		selectNextTrip();
		if( n == mSelectedTrip )
		{
			mSelectedTrip = m;
			return null;
		}
		jTrip out = getSelectedTrip();
		mSelectedTrip = m;
		return out;
	}
	
	// TODO: ArrayList<jStop> should be of jTripSeq, not jTrip.
	public jBoundsE6 getBounds()
	{
		Iterator<jTrip> i = mTrips.iterator();
		if( !i.hasNext() ) return null;
		return i.next().getBounds();
	}

	void sortTripsByTime()
	{
		int z = mTrips.size();
		int a, b, c, d;
		jTrip t;
		
		for( d = 1; d < z; d++ )
		{
			t = mTrips.get(d);
			a = 0;
			c = d;
			while( a < c )
			{
				b = (a+c)/2;
				if( t.compareTime(mTrips.get(b)) < 0 ) { c = b; }
				else { a = b+1; }
			}
			for( b = d; b > a; b-- )
				mTrips.set(b,mTrips.get(b-1));
			mTrips.set(a,t);
		}
	}

	void drawSeqLines(jStop stopRef, Canvas c, Projection p, Point reuse, int zoom)
	{	
	//	selectTripForNow(stopRef);
	//	jTrip t = getSelectedTrip();
	//	if( t == null ) return;
		
		if( mTrips == null ) return;
		jTrip t = mTrips.get(0);
		if( t == null || t.getStops() == null ) return;
		
		Iterator<jStop> i = t.getStops().iterator(); if( !i.hasNext() ) return;
		jStop a = i.next(); if( a == null ) return;
		p.toPixels(a.getPoint(),reuse);
		float x = reuse.x, y = reuse.y;
		c.drawCircle(x, y, 3, oStopsOverlay.COLOR_HILIGHT);
		
		while( i.hasNext() )
		{
			a = i.next(); if( a == null ) continue;
			p.toPixels(a.getPoint(),reuse);
			c.drawLine(x,y,reuse.x,reuse.y,oStopsOverlay.COLOR_STOP);
			x = reuse.x; y = reuse.y;
			c.drawCircle(x, y, 3, oStopsOverlay.COLOR_HILIGHT);
		}
	}
}
