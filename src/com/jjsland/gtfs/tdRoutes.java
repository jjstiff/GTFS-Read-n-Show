package com.jjsland.gtfs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class tdRoutes implements ListAdapter {
	public final static String ASSET_FILE = "gtfs_vta/routes.txt";

	public final static String C_COUNT = "_COUNT";
	public final static String C_LONG_NAME = "route_long_name";
	public final static String C_TYPE = "route_type";
	public final static String C_TEXT_COLOR = "route_text_color";
	public final static String C_COLOR = "route_color";
	public final static String C_AGENCY = "agency_id";
	public final static String C_ID = "route_id";
	public final static String C_URL = "route_url";
	public final static String C_DESC = "route_desc";
	public final static String C_SHORT_NAME = "route_short_name";

	final static int I_COUNT = 1;
	final static int I_LONG_NAME = 2;
	final static int I_TYPE = 3;
	final static int I_TEXT_COLOR = 4;
	final static int I_COLOR = 5;
	final static int I_AGENCY = 6;
	final static int I_ID = 7;
	final static int I_URL = 8;
	final static int I_DESC = 9;
	final static int I_SHORT_NAME = 10;
	final static int I_UNKNOWN = 11;
	
	ArrayList<jRoute> mById;
	//Context mContext;
	LayoutInflater mInflater;
	
	public tdRoutes(tdDownloader d) throws Exception {
		mById = new ArrayList<jRoute>(50);
		//mContext = d.mContext;
		mInflater = (LayoutInflater) gtfsApp.getStaticContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		readData(d);
	}
	
	public void trimToSize()
	{
		mById.trimToSize();
		Iterator<jRoute> i = mById.iterator();
		while(i.hasNext())
		{
			i.next().trimToSize();
		}
	}
	
	public void normalize()
	{
		Iterator<jRoute> i = mById.iterator();
		while( i.hasNext() )
		{
			jRoute r = i.next();
			if( r != null ) 
				r.normalize();
		}
	}
	public void combineDuplicateSequences()
	{
		Iterator<jRoute> i = mById.iterator();
		while(i.hasNext())
		{
			jRoute r = i.next();
			if( r != null )
				r.combineDuplicateSequences();
		}
	}
	public String toString()
	{
		if( mById == null || mById.size() <= 0 ) return "Routes contain no data.";
		return "Routes contain " + mById.size() + " entries.";
	}
	
	public void clearMemory()
	{
	    if( mById != null ) { mById.clear(); mById = null; }
	}
	
	int[] parseHead(String line)
	{
		String[] d = line.split(",");
		int size = d.length;
		int[] h = new int[size];
		for( int i = 0; i < size; i++ )
		{
			d[i] = GTFSLoader.removeQuotes(d[i]);
			if( d[i].compareTo(C_LONG_NAME) == 0 ) h[i] = I_LONG_NAME;
			else if( d[i].compareTo(C_TYPE) == 0 ) h[i] = I_TYPE;
			else if( d[i].compareTo(C_TEXT_COLOR) == 0 ) h[i] = I_TEXT_COLOR;
			else if( d[i].compareTo(C_COLOR) == 0 ) h[i] = I_COLOR;
			else if( d[i].compareTo(C_AGENCY) == 0 ) h[i] = I_AGENCY;
			else if( d[i].compareTo(C_ID) == 0 ) h[i] = I_ID;
			else if( d[i].compareTo(C_URL) == 0 ) h[i] = I_URL;
			else if( d[i].compareTo(C_DESC) == 0 ) h[i] = I_DESC;
			else if( d[i].compareTo(C_SHORT_NAME) == 0 ) h[i] = I_SHORT_NAME;
			else h[i] = I_UNKNOWN;
		}
		return h;
	}
	
	jRoute parseRoute(int[] head, String line) throws NumberFormatException
	{
		String[] d = line.split(",");
		int size = (head.length<d.length) ? head.length : d.length;
		
		int id = 0;
		String agency = null;
		String longname = null;
		String shortname = null;
		
		
		for( int i = 0; i < size; i++ )
		{
			d[i] = GTFSLoader.removeQuotes(d[i]);
			switch(head[i]) {
			case I_ID:			id = Integer.parseInt(d[i]); break;
			case I_AGENCY:		agency = d[i]; break;
			case I_LONG_NAME:	longname = d[i]; break;
			case I_SHORT_NAME:	shortname = d[i]; break;
			}
		}
		
		return new jRoute(id,agency,longname,shortname);
	}
	
	void addByID(jRoute t)
	{
		if( mById == null ) { mById = new ArrayList<jRoute>(50); }
		
		int a = 0, c = mById.size()-1;
		if( c < 0 ) { mById.add(t); return; }
		if( t.compareId(mById.get(a)) < 0 )
			{ mById.add(a,t); return; }
		if( t.compareId(mById.get(c)) > 0 )
			{ mById.add(c+1,t); return; }
		
		while( a < c )
		{
		   int b = (a+c)/2;
		   int v = t.compareId(mById.get(b));
		   if( v < 0 ) { c = b; }
		   else { a = b+1; }
		}
		mById.add(c,t);
	}
	
	public void readData(tdDownloader d) throws Exception
	{
		try {
			jZipData z = d.getRoutes();
			InputStreamReader ir = new InputStreamReader(z.mStream);
			BufferedReader r = new BufferedReader(ir,256);
			String s = r.readLine(); // First line contains the field names
			int[] head = parseHead(s);
			
			while( (s = r.readLine()) != null )
			{
				jRoute t = parseRoute(head,s);
				addByID(t);
			}
			z.mStream.close();
		}
		catch (Exception e) {
			throw new Exception("Error reading route file.", e);
		}
	}
	
	public jRoute findByID(int rId)
	{
		int a = 0, c = mById.size()-1;
		jRoute r;
		while( a < c )
		{
			int b = (a+c)/2;
			r = mById.get(b);
			if( r.mId == rId ) return r;
			if( r.mId < rId ) a = b+1;
			else c = b-1;
		}
		r = mById.get(a);
		return (r.mId == rId ? r : null);
	}

	public void sortTripsByTime()
	{
		Iterator<jRoute> i = mById.iterator();
		while(i.hasNext())
		{
			jRoute r = i.next();
			if( r != null )
				r.sortTripsByTime();
		}
	}
	
	static int countTripsForWhen(ArrayList<jTrip> trips, int when)
	{
		int count = 0;
		Iterator<jTrip> i = trips.iterator();
		while(i.hasNext())
		{
			if( i.next().matchesWhen(when) )
				count++;
		}
		return count;
	}
	
	/**
	 * ListAdapter Interface Items
	 * 
	 */
	public boolean areAllItemsEnabled() { return true; }
	public boolean isEnabled(int p) { return 0 <= p && p < mById.size(); }
	public int getCount() { return (mById == null) ? 0 : mById.size(); }
	public Object getItem(int p)
	{
		if( 0 <= p && p < mById.size() )
			return mById.get(p);
		return null;
	}
	public long getItemId(int p) { return p; }
	public int getItemViewType(int p) { return 0; }
	public View getView(int p, View cView, ViewGroup parent)
	{
		jRoute r = mById.get(p);
		
		if(cView == null)
			cView = mInflater.inflate(R.layout.showschedule_row,null);
		
		TextView rNum = (TextView) cView.findViewById(R.id.ssRouteNum);
		TextView rAgen = (TextView) cView.findViewById(R.id.ssAgency);
		TextView rName = (TextView) cView.findViewById(R.id.ssLongName);
		TextView rCount1 = (TextView) cView.findViewById(R.id.ssTripCount1);
		TextView rCount2 = (TextView) cView.findViewById(R.id.ssTripCount2);
		TextView rCount3 = (TextView) cView.findViewById(R.id.ssTripCount3);
		rNum.setText(Integer.toString(r.mId));
		rAgen.setText(jCalendar.whenToString(jCalendar.getWhenToShow()));
		rName.setText(r.mLongName);
		rCount1.setText(r.aTripSeq.size() + " Sequences");
		int trips = 0;
		int stops = 0;
		for( int i = 0; i < r.aTripSeq.size(); i++ )
		{
			jTripSeq jts = r.aTripSeq.get(i);
			Iterator<jTrip> jt = jts.mTrips.iterator();
			while(jt.hasNext())
			{
				jTrip t = jt.next();
				if( t == null || !jCalendar.showTrip(t) )
					continue;
				int s = t.getStopCount();
				stops = (s>stops?s:stops);
				trips++;
			}
		}
		rCount2.setText(trips + " Trips");
		rCount3.setText(stops + " Stops");
		return cView;
	}
	public int getViewTypeCount() { return 1; }
	public boolean hasStableIds() { return true; }
	public boolean isEmpty() { return mById.size() == 0; }
	public void registerDataSetObserver(DataSetObserver o) {}
	public void unregisterDataSetObserver(DataSetObserver o) {}
	
}
