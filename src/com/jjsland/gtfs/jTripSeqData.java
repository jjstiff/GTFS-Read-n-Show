package com.jjsland.gtfs;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.android.maps.GeoPoint;

public class jTripSeqData {

	ArrayList<jStop> mStops;
	ArrayList<Integer> mTimes;  // [0] should have time = 0.
	ArrayList<Integer> mEstimatedTimes;  // [0] should have time = 0.
	jBoundsE6 mBounds;
	jRoute mRoute;
	
	public jTripSeqData(jRoute route)
	{
		mStops = new ArrayList<jStop>(10);
		mTimes = new ArrayList<Integer>(10);
		mBounds = null;
		mRoute = route;
	}
	
	public jRoute getRoute() { return mRoute; }
	
	public void trimToSize()
	{
		mStops.trimToSize();
		mTimes.trimToSize();
	}
	
	/**
	 * 
	 * @param stop - this method does nothing if stop is null.
	 * @param time - seconds in day for the arrival time
	 * @param seq - sequence number for the stop-time
	 */
	public void addStopTime(jStop stop, int seq, int time)
	{
		if( stop == null ) return;
		int i;
		for( i = mStops.size(); i < seq; i++ )
			mStops.add(null);
		for( i = mTimes.size(); i < seq; i++ )
			mTimes.add(null);
		seq--;
		mStops.set(seq,stop);
		mTimes.set(seq,(time > 0 ? new Integer(time) : null));
		if( mBounds == null )
			mBounds = new jBoundsE6(stop.getPoint());
		else
			mBounds.add(stop.getPoint());
	}
	
	public jStop getStop( int index )
	{
		if( index < 0 || index >= mStops.size() ) return null;
		return mStops.get(index);
	}
	public int getStopCount() { return mStops.size(); }

	public String getTimeString( int index, int deltaTime )
	{
		if( index < 0 || index >= mTimes.size() ) return "--:--e";
		Integer t = mTimes.get(index);
		if( t == null ) return getEstimatedTimeString( index, deltaTime );
		return "." + tdStopTimes.timeToString(deltaTime + t.intValue());
	}
	
	public String getEstimatedTimeString( int index, int deltaTime )
	{
		if( mEstimatedTimes == null )
			estimateUnknownTimes();
		if( mEstimatedTimes == null ||
			index < 0 || index >= mEstimatedTimes.size() )
			return "--:--f";
		Integer t = mEstimatedTimes.get(index);
		if( t == null ) return "--:--g";
		return tdStopTimes.timeToString(deltaTime + t.intValue());
	}
	
	public int getStopTime_seconds(jStop inStop)
	{
		if( inStop == null )
		{
			Iterator<Integer> i = mTimes.iterator();
			while( i.hasNext() )
			{
				Integer j = i.next();
				if( j != null ) return j.intValue();
			}
			return 0;
		}
		int i = 0;
		for( i = 0 ; i < mStops.size(); i++ )
		{
			if( mStops.get(i) == inStop ) break;
		}
		if( i >= mStops.size() ) return 0;
		
		if( mEstimatedTimes == null )
			estimateUnknownTimes();
		if( mEstimatedTimes == null )
			return 0;
		
		Integer t;
		for( t = mEstimatedTimes.get(i); i >= 0; i-- )
		{
			if( t != null ) return t.intValue();
		}
		return 0;
	}

	int findClosestStopIndex( int latE6, int lonE6 )
	{
		Iterator<jStop> j = mStops.iterator();
		long closest = -1;
		int outIndex = -1;
		int curIndex = 0;
		
		while(j.hasNext())
		{
			jStop p = j.next();
			long a = latE6-p.getLatE6();
			long b = lonE6-p.getLonE6();
			long v = a*a + b*b;
			if( closest < 0 || v < closest )
			{
				outIndex = curIndex;
				closest = v;
			}
			curIndex++;
		}
		return outIndex;
	}

	
	jStop getStopAtIndex( int index )
	{
		if( index < 0 || index >= mStops.size() ) return null;
		return mStops.get(index);
	}
	
	public void estimateUnknownTimes()
	{
		jStop s;
		GeoPoint pa, pb, pi;
		Integer ta, tb, ti;
		int a, b, i;
		long total_dist = 0;
		int delta_time;
		
		if( mEstimatedTimes == null )
			mEstimatedTimes = new ArrayList<Integer>(mTimes.size());
		for( i = mEstimatedTimes.size(); i < mTimes.size(); i++ )
			mEstimatedTimes.add(null);
		
		a = b = i = 0;
		ta = mTimes.get(a);
		s = mStops.get(a);
		if( ta == null || s == null ) return;
		pa = s.getPoint();
		for( i = 1; i < mStops.size(); i++ )
		{
			s = mStops.get(i); if( s == null ) continue;
			pi = s.getPoint();
			ti = mTimes.get(i);
			if( ti != null )
			{
				a = b = i;
				pa = pi;
				ta = ti;
				mEstimatedTimes.set(i,ti);
				continue;
			}
			if( a == b )
			{
				total_dist = 0;
				pb = pa;
				for( b++; b < mStops.size() && b < mTimes.size() && mTimes.get(b) == null; b++ )
				{
					s = mStops.get(b);
					if( s != null )
					{
						total_dist += dist_squared(pb,s.getPoint());
						pb = s.getPoint();
					}
				}
				if( b >= mTimes.size() || b >= mStops.size() ) return;
				s = mStops.get(b); if( s == null ) return;
				pb = s.getPoint();
			}
			if(total_dist <= 0) continue;
			tb = mTimes.get(b); if( tb == null ) return;
			delta_time = tb.intValue() - ta.intValue();
			int time = (int)((float)delta_time * dist_squared(pi,pa) / total_dist);
			pa = pi;
			ta += time;
			if( i >= mEstimatedTimes.size() ) break;
			mEstimatedTimes.set(i, ta);
		}
	}
	
	long dist_squared(GeoPoint a, GeoPoint b)
	{
		long lat = a.getLatitudeE6()-b.getLatitudeE6();
		long lon = a.getLongitudeE6()-b.getLongitudeE6();
		return lat*lat + lon*lon;
	}
	
	/**
	 * Normalize the stop-time data: meaning that mTimes[0] = 0 and mTimes[x] = mTimes[x] - mTimes[0];
	 * @return the old value of mTimes[0].
	 */
	public int normalize()
	{
		if( mTimes == null || mTimes.size() <= 0 ) return 0;
		Integer t = mTimes.get(0); if( t == null || t <= 0 ) return 0;
		int tv = t.intValue();
		
		for( int i = 1; i < mTimes.size(); i++ )
		{
			Integer u = mTimes.get(i);
			if( u != null )
				mTimes.set(i,new Integer(u.intValue()-t));
		}
		return tv;
	}
	
	/*
	public boolean matchesSeq(jTripSeqData cmp)
	{
		Iterator<jStop> i = mStops.iterator();
		Iterator<jStop> j = cmp.mStops.iterator();
		while( i.hasNext() && j.hasNext() )
		{
			if( i.next() != j.next() )
				return false;
		}
		if( i.hasNext() || j.hasNext() ) return false;	
		return true;
	}*/
	
	/*
	@Override
	public boolean equals(Object d)
	{
		if( !(d instanceof jTripSeqData) )
			return false;
		return equals((jTripSeqData)d);
	}
	*/
	public boolean equals(jTripSeqData d)
	{
		if( d == null ) return false;
		if( d == this ) return true;
		if( !mBounds.equals(d.mBounds) ) return false;
		
		Iterator<jStop> i = mStops.iterator();
		Iterator<jStop> j = d.mStops.iterator();
		while( i.hasNext() && j.hasNext() )
		{
			if( i.next() != j.next() )
				return false;
		}
		if( i.hasNext() || j.hasNext() ) return false;
		return true;
		/*
		Iterator<Integer> t = mTimes.iterator();
		Iterator<Integer> u = d.mTimes.iterator();
		while( t.hasNext() && u.hasNext() )
		{
			Integer tt = t.next();
			Integer uu = u.next();
			if( tt == null && uu == null ) continue;
			if( tt == null || uu == null ) return false;
			if( tt.intValue() != uu.intValue() ) return false;
		}
		if( t.hasNext() || u.hasNext() ) return false;
		
		return true;*/
	}
	
	
	public jBoundsE6 getBounds()
	{
		return mBounds;
	}
	
	public int getStopIndex(jStop s)
	{
		for( int i = 0; i < mStops.size(); i++ )
		{
			if( mStops.get(i) == s ) return i;
		}
		return -1;
	}

}
