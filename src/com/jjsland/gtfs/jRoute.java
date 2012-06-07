package com.jjsland.gtfs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;


public class jRoute
{
	// Discovered from tdRoutes
	int mId;
	String mLongName;
	String mShortName;
	String mAgency;
	
	// Discovered from tdTrips
	ArrayList<jTripSeq> aTripSeq;
	
	// Used for display
	int mSelectedSeq;
	public void incSelectedSeq()
	{
		mSelectedSeq++;
		if( mSelectedSeq >= aTripSeq.size() || mSelectedSeq < 0 )
			mSelectedSeq=0;
	}
	public void decSelectedSeq()
	{
		mSelectedSeq--;
		if( mSelectedSeq < 0 || mSelectedSeq >= aTripSeq.size() )
			mSelectedSeq = aTripSeq.size()-1;
	}
	public jTripSeq getSelectedSeq()
	{
		if( mSelectedSeq < 0 || mSelectedSeq > aTripSeq.size() )
			incSelectedSeq();
		return aTripSeq.get(mSelectedSeq);
	}
	public String getSelectedSeqName()
	{
		jTripSeq jts = getSelectedSeq();
		return (jts == null ? "No Sequence Error" : jts.mSeqName);
	}
	
	public ArrayList<jTripSeq> getSequences() { return aTripSeq; }
	
	public jRoute(int id,String agency,String longname,String shortname)
	{ 
		mId = id;
		mAgency = agency;
		mLongName = longname;
		mShortName = shortname;
		aTripSeq = new ArrayList<jTripSeq>(2);
		mSelectedSeq = 0;
	}
	
	public void trimToSize()
	{
		aTripSeq.trimToSize();
		Iterator<jTripSeq> i = aTripSeq.iterator();
		while(i.hasNext()) i.next().trimToSize();
	}
	
	public void normalize()
	{
		Iterator<jTripSeq> i = aTripSeq.iterator();
		while( i.hasNext() )
		{
			jTripSeq ts = i.next();
			if( ts != null )
				ts.normalize();
		}
	}
	
	public void combineDuplicateSequences()
	{
		Iterator<jTripSeq> i = aTripSeq.iterator(), j;
		ArrayList<jTripSeq> newSeq = new ArrayList<jTripSeq>();
		while(i.hasNext())
		{
			jTripSeq ts = i.next();
			if( ts != null )
			{
				ArrayList<jTripSeq> more = ts.combineDuplicateSequences();
				if( more != null )
				{
					j = more.iterator();
					while(j.hasNext())
						newSeq.add(j.next());
				}
			}
		}
		j = newSeq.iterator();
		while( j.hasNext() )
		{
			aTripSeq.add(j.next());
		}
		newSeq.clear();
	}
	
	public int compareId(jRoute t) { return mId-t.mId; }
	public int compareId(int id) { return mId-id; }
	
	String getNextTimesAtStop(int stopID, int count)
	{
		return "Not displaying times yet.";
	}
	
	String getNext3TimesString(jStop stop)
	{
		Iterator<jTripSeq> i = aTripSeq.iterator();
		String out = "";
		while( i.hasNext() )
		{
			jTripSeq s = i.next();
			if( s != null && s.containsStop(stop) )
			{
				// TODO: Duplicating work on s.containsStop/getStopIndex.
				out += s.getNext3TimesString(stop) + "\n";
			}
		}
		return out;
	}
	
	/**
	 * 
	 * @param name the name by which this trip is identified: identically named trips shall be grouped together, the return value will specify which group.
	 * @param t the trip to add.
	 * @return the index in mTripNames by which this trip is identified
	 */
	public void addTripInSeqById(String name, jTrip t)
	{
		Iterator<jTripSeq> i = aTripSeq.iterator();
		jTripSeq s = null;
		while( i.hasNext() )
		{
			jTripSeq j = i.next();
			if( j.mSeqName.matches(name) )
			{
				s = j;
				break;
			}
		}
		if( s == null )
		{
			s = new jTripSeq(name);
			aTripSeq.add(s);
		}
		s.addTripById(t);
	}
	
	public void sortTripsByTime()
	{
		Iterator<jTripSeq> i = aTripSeq.iterator();
		while(i.hasNext())
		{
			jTripSeq ts = i.next();
			if( ts != null )
				ts.sortTripsByTime();
		}
	}
	
	/*
	private boolean addStopIfNotExist( ArrayList<jStop> arr, jStop stop, int sequ )
	{
		if( sequ <= 0 ) return false;
		
		for( int i = arr.size(); i < sequ; i++ )
			arr.add(null);
		
        if( arr.size() < sequ ) return false;
        
		jStop s = arr.get(sequ-1);
		if( s == null )
		{
			arr.set(sequ-1,stop);
			stop.addRoute(this);
			return true;
		}
		else return ( s == stop );
	}*/
	
	/**
	 * Add a stop with the sequence number to this route for the given direction.
	 * @param stop
	 * @param dir
	 * @param sequ
	 * @return
	 */
	/*
	public boolean addStop(jStop stop, jTrip trip, int sequ)
	{
		if( trip.mDir == 1 )
			return addStopIfNotExist(mDir1,stop,sequ);
		else if( trip.mDir == 2 )
			return addStopIfNotExist(mDir2,stop,sequ);
		else
			return false;
	}*/
	/*
	int mSelectDir;
	int mSelectSequ;
	public boolean selectStop(jStop s)
	{
		int i;
		if( mDir1 != null ) for( i = 0; i < mDir1.size(); i++ )
		{
			if( mDir1.get(i) == s )
			{
				mSelectDir = 1;
				mSelectSequ = i+1;
				return true;
			}
		}
		if( mDir2 != null ) for( i = 0; i < mDir2.size(); i++ )
		{
			if( mDir2.get(i) == s )
			{
				mSelectDir = 2;
				mSelectSequ = i+1;
				return true;
			}
		}
		mSelectDir = -1;
		mSelectSequ = -1;
		return false;
	}
	public int getSequNum() { return mSelectSequ; }
	public int getDir() { return mSelectDir; }
	public String getDirString() 
	{
		if( mSelectDir == 1 ) return mNameDir1;
		if( mSelectDir == 2 ) return mNameDir2;
		return "";
	}
	*/
	/*
	public String getTimes(int whenToDisplay)
	{
		Iterator<jTrip> i;
		if( mSelectDir == 1 && mTripsDir1 != null )
			i = mTripsDir1.iterator();
		else if( mSelectDir == 2 && mTripsDir2 != null )
			i = mTripsDir2.iterator();
		else
			return "No Trips.";
		
		jTrip j;
		Integer t;
		int v;
		int delta_sequ;
		int delta;
		String out = "";
		boolean first = true;
		
		while(i.hasNext())
		{
			delta_sequ = mSelectSequ;
			delta = 0;
			j = i.next();
			if(!j.matchesWhen(whenToDisplay) || j.mTimes.size() <= delta_sequ ) continue;
			
			do {
				t = j.mTimes.get(delta_sequ);
				v = (t == null) ? -1 : t.intValue();
				if( v <= 0 )
				{
					delta_sequ--;
					delta++;
				}
			} while( delta_sequ >= 0 && v <= 0 );
			
			if( delta_sequ < 0 )
			{
				delta_sequ = mSelectSequ;
				delta = 0;
				v = -1;
				while( delta_sequ < j.mTimes.size()  && v <= 0 )
				{
					t = j.mTimes.get(delta_sequ);
					v = (t == null) ? -1 : t.intValue();
					if( v <= 0 )
					{
						delta_sequ++;
						delta--;
					}
				}
			}
			//if( !hasHappened(v) )
			{
				if( !first ) out += ", ";
				if( delta > 0 ) out += "(";
				out += timeString(v);
				if( delta > 0 )
				{
					out += ")+" + delta ;
					if( first ) out += " stops";
				}
				else if( delta < 0 )
				{
					out += ")-" + (-delta) ;
					if( first ) out += " stops";
				}
				first = false; 
			}
		}
		
		if( first ) return "No More Trips Today.";
		
		return out;
	}
	*/
	
	static String timeString(int v)
	{
		int h = v/(60*60);
		int m = (v/60)%60;
		String H = Integer.toString(1+((h+11)%12));
		String M = (m < 10) ? "0" + m : Integer.toString(m);
		return H + ":" + M + ((12<=h && h<24)?"p":"a");
	}
	
	static final int MINUTES_OF_DELAY = 15;
	static boolean hasHappened(int time)
	{
		Calendar now = Calendar.getInstance();
		int hour = 60*60*now.get(Calendar.HOUR_OF_DAY);
		int min = 60*(now.get(Calendar.MINUTE)-MINUTES_OF_DELAY);
		return time < hour + min;
	}
	
	int findClosestStopIndex( int latE6, int lonE6 )
	{
		jTripSeq s = getSelectedSeq(); if( s == null ) return -1;
		jTrip t = s.mTrips.get(0); if( t == null ) return -1;
		return t.findClosestStopIndex( latE6, lonE6 );
	}
	
	jStop findClosestStop(int latE6, int lonE6 )
	{
		int i = findClosestStopIndex(latE6,lonE6);
		return getStopAtIndex(i);
	}
	
	jStop getStopAtIndex( int index )
	{
		jTripSeq s = getSelectedSeq(); if( s == null ) return null;
		jTrip t = s.mTrips.get(0); if( t == null ) return null;
		return t.getStopAtIndex(index);
	}
	
	jBoundsE6 mBounds = null;
	jBoundsE6 getBounds()
	{
		if( mBounds != null ) return mBounds;
    	Iterator<jTripSeq> i = aTripSeq.iterator();
    	if( !i.hasNext() ) return null;
    	mBounds = i.next().getBounds();
    	while( i.hasNext() )
    		mBounds.add(i.next().getBounds());
    	return mBounds;
	}
}
