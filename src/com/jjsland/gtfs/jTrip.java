package com.jjsland.gtfs;

import java.util.ArrayList;
import java.util.Iterator;

public class jTrip
{
	int mId;
	jCalendar mServ;
	int mStartTime; // Seconds in day to add to the sequence data.
	jTripSeqData mSeq;
	
	public jTrip(int tripID, jCalendar serv, jRoute route)
	{
		mId = tripID;
		mServ = serv;
		mSeq = new jTripSeqData(route);
	}
	
	public void trimToSize()
	{
		mSeq.trimToSize();
	}
	
	public jRoute getRoute() { return mSeq.getRoute(); }
	
	public int compareId(jTrip t) { return mId-t.mId; }
	public int compareId(int id) { return mId-id; }
	
	public int getStopTime_seconds(jStop inStop)
	{
		return mSeq.getStopTime_seconds(inStop)+mStartTime;
	}
	
	public int findClosestStopIndex( int latE6, int lonE6 )
	{
		return mSeq.findClosestStopIndex( latE6, lonE6 );
	}

	
	jStop getStopAtIndex( int index )
	{
		return mSeq.getStopAtIndex(index);
	}
	
	
	public int compareTime(jTrip t)
	{
		if( t == null ) return 1;
		if( mStartTime != t.mStartTime )
			return mStartTime - t.mStartTime;
		
		Iterator<Integer> a = mSeq.mTimes.iterator();
		Iterator<Integer> b = t.mSeq.mTimes.iterator();
		while( a.hasNext() && b.hasNext() )
		{
			Integer c = a.next();
			Integer d = b.next();
			if( c == null && d == null ) continue;
			if( c == null ) return -1;
			if( d == null ) return 1;
			if( c.intValue() != d.intValue() ) return c-d;
		}
		if( a.hasNext() ) return 1;
		else if( b.hasNext() ) return -1;
		else return 0;
	}
	
	public void normalize()
	{
		int dt = mSeq.normalize();
		if( dt > 0 )
			mStartTime = dt;
	}
	
	public boolean matchesSeq(jTrip cmp)
	{
		if( cmp == null ) return false;
		return mSeq.equals(cmp.mSeq);
	}
	
	public void addStopTime(jStop stop, int sequ, int time)
	{
		mSeq.addStopTime(stop, sequ, time);
	}
	
	/**
	 * 
	 * @param when uses the jCalendar constants
	 * @return
	 */
	public boolean matchesWhen(int when)
	{
		return mServ.matchesWhen(when);
	}
	
	public ArrayList<jStop> getStops() { return mSeq.mStops; }
	public jStop getStop( int index ) { return mSeq.getStop(index); }
	public int getStopCount() { return mSeq.getStopCount(); }
	public String getTimeString( int index )
	{
		return mSeq.getTimeString( index, mStartTime );
	}
	
	public jBoundsE6 getBounds()
	{
		return mSeq.getBounds();
	}
	
	public int getStopIndex(jStop s)
	{
		return mSeq.getStopIndex(s);
	}
}
