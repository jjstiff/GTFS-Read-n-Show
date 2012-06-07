package com.jjsland.gtfs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;

public class tdStops implements ListAdapter
{
	public final static String ASSET_FILE = "gtfs_vta/stops.txt";
	
	public final static String C_COUNT = "_COUNT";
	public final static String C_LAT = "stop_lat";
	public final static String C_ZID = "zone_id";
	public final static String C_LON = "stop_lon";
	public final static String C_ID = "stop_id";
	public final static String C_DESC = "stop_desc";
	public final static String C_NAME = "stop_name";
	public final static String C_TYPE = "location_type";

	final static int I_COUNT = 1;
	final static int I_LAT = 2;
	final static int I_ZID = 3;
	final static int I_LON = 4;
	final static int I_ID = 5;
	final static int I_DESC = 6;
	final static int I_NAME = 7;
	final static int I_TYPE = 8;
	final static int I_UNKNOWN = 9;
	
	ArrayList<jStop> mById;
	ArrayList<jStop> mByLoc;
	
	private jBoundsE6 mBounds;
	
	public tdStops(tdDownloader d) throws Exception
	{
		mById = new ArrayList<jStop>(4000);
		mByLoc = new ArrayList<jStop>(4000);
		readData(d);
	}
	
	public void trimToSize()
	{
		mById.trimToSize();
		mByLoc.trimToSize();
	}
	
	public String toString()
	{
		if( mById == null || mById.size() <= 0 ) return "Stops contains no data.";
		String out = "Stops contains " + mById.size() + " entries.";
		out += "\nLow Lat: " + mBounds.getLowLatE6();
		out += "\nLow Lon: " + mBounds.getLowLonE6();
		out += "\nHigh Lat: " + mBounds.getHighLatE6();
		out += "\nHigh Lon: " + mBounds.getHighLonE6();
		return out;
	}
	
	public int getSize() { return (mById == null) ? 0 : mById.size(); }
	
	public jStop getElementAt(int i) { return mById.get(i); }
	
	public void clearMemory()
	{
	    if( mById != null ) { mById.clear(); mById = null;  }
	    if( mByLoc != null ) { mByLoc.clear(); mByLoc = null; }
	}
	int[] parseHead(String line)
	{
		String[] d = line.split(",");
		int size = d.length;
		int[] h = new int[size];
		for( int i = 0; i < size; i++ )
		{
			d[i] = GTFSLoader.removeQuotes(d[i]);
			if( d[i].compareTo(C_LAT) == 0 ) h[i] = I_LAT;
			else if( d[i].compareTo(C_ZID) == 0 ) h[i] = I_ZID;
			else if( d[i].compareTo(C_LON) == 0 ) h[i] = I_LON;
			else if( d[i].compareTo(C_ID) == 0 ) h[i] = I_ID;
			else if( d[i].compareTo(C_DESC) == 0 ) h[i] = I_DESC;
			else if( d[i].compareTo(C_NAME) == 0 ) h[i] = I_NAME;
			else if( d[i].compareTo(C_TYPE) == 0 ) h[i] = I_TYPE;
			else h[i] = I_UNKNOWN;
		}
		return h;
	}
	
	jStop parseStop(int[] head, String line) throws NumberFormatException
	{
		String[] d = line.split(",");
		int size = (head.length<d.length) ? head.length : d.length;
		
		int id = 0;
		String name = null;
		float lat = 0.0f;
		float lon = 0.0f;
		
		for( int i = 0; i < size; i++ )
		{
			d[i] = GTFSLoader.removeQuotes(d[i]);
			switch(head[i]) {
			case I_LAT:		lat = (float)Double.parseDouble(d[i]); break;
			case I_LON:		lon = (float)Double.parseDouble(d[i]); break;
			case I_ID:		id = Integer.parseInt(d[i]); break;
			case I_NAME:	name = d[i]; break;
			}
		}
		
		jStop out = new jStop(id,name,(int)(lat*1e6),(int)(lon*1e6));
		if( mBounds == null )
			mBounds = new jBoundsE6(out.getPoint());
		else
			mBounds.add(out.getPoint());
		
		return out;
	}
	
	public jBoundsE6 getBounds() { return mBounds; }
	
	void addByID(jStop t)
	{
		if( mById == null ) { mById = new ArrayList<jStop>(3000); }
		
		int a = 0, c = mById.size()-1;
		if( c < 0 ) { mById.add(t); return; }
		if( t.compareId(mById.get(a)) < 0 )
			{ mById.add(a,t); return; }
		if( t.compareId(mById.get(c)) > 0 )
			{ mById.add(c+1,t); return; }
		
		boolean exist = false;
		while( a < c )
		{
		   int b = (a+c)/2;
		   int v = t.compareId(mById.get(b));
		   if( v < 0 ) { c = b; }
		   else if( v > 0 ) { a = b+1; }
		   else { exist = true; a = c = b; }
		}
		if( !exist ) mById.add(c,t);
	}
	
	int cache = -1;
	jStop findById(int id)
	{
		int a = 0, c = mById.size()-1;
		int b,v;
		jStop s;
		if( cache >= 0 )
		{
			s = mById.get(cache);
			v = s.compareId(id);
			if( v == 0 ) return s;
			if( v > 0 ) c = cache;
			else a = cache+1;
		}
		while( a < c )
		{
			b = (a+c)/2;
			s = mById.get(b);
			v = s.compareId(id);
			if( v == 0 ) { cache = b; return s; }
			if( v > 0 ) c = b;
			else a = b+1;
		}
		return null;
	}
	
	void addByLocation(jStop t)
	{
		if( mByLoc == null ) { mByLoc = new ArrayList<jStop>(3000); }
	
		int a = 0, c = mByLoc.size()-1;
		if( c <= 0 ) { mByLoc.add(t); return; }
		if( t.compareLoc(mByLoc.get(a)) < 0 )
			{ mByLoc.add(a,t); return; }
		if( t.compareLoc(mByLoc.get(c)) > 0 )
			{ mByLoc.add(c+1,t); return; }
	
		while( a < c )
		{
			int b = (a+c)/2;
			int v = t.compareLoc(mByLoc.get(b));
			if( v < 0 ) { c = b; }
			else { a = b+1; }
		}
		mByLoc.add(c,t);	
	}
	
	/**
     * return the index of the first item in mByLoc that is
     * where latitude >= inLat
     * O(log n);
	 */
	int findIndexByLat(int inLatE6)
	{
		int a = 0, c = mByLoc.size()-1;

		while( a < c )
		{
			int b = (a+c)/2;
			if( inLatE6 < mByLoc.get(b).getLatE6() ) { c = b; }
			else { a = b+1; }
		}
		return a;
	}
	
	ArrayList<jStop> getStopddsInView(ArrayList<jStop> reuse, jBoundsE6 b, double lat_low, double lon_low, double lat_high, double lon_high)
	{
		if( reuse == null )
			reuse = new ArrayList<jStop>(50);
		reuse.clear();
		
		int size = mByLoc.size();
		for( int i = findIndexByLat(b.getLowLatE6()); i < size; i++ )
		{
			jStop s = mByLoc.get(i);
			if( s.getLatF() < lat_low ) continue;
			if( s.getLatF() > lat_high ) break;
			if( s.getLonF() <= lon_high && s.getLonF() >= lon_low)
				addByRouteCountDesc(reuse,s);
		}
		return reuse;
	}
	
	void addByRouteCountDesc(ArrayList<jStop> list, jStop s)
	{
		int a = 0, c = list.size()-1;
		int cnt = s.mRoutes.size();
		while( a < c )
		{
			int b = (a+c)/2;
			int cmp = list.get(b).mRoutes.size();
			if( cnt > cmp ) c = b;
			else if( cnt < cmp ) a = b+1;
			else a = c = b;
		}
		list.add(a,s);
	}
	
	public void readData(tdDownloader d) throws Exception
	{
		mBounds = null; // Prepare the window for the data set.
		try {
			jZipData z = d.getStops();
			InputStreamReader ir = new InputStreamReader(z.mStream);
			BufferedReader r = new BufferedReader(ir,256);
			String s = r.readLine(); // First line contains the field names
			int[] head = parseHead(s);
			
			while( (s = r.readLine()) != null )
			{
				jStop t = parseStop(head,s);
				addByLocation(t);
				addByID(t);
			}
			z.mStream.close();
		}
		catch (Exception e) {
			throw new Exception("Could not read Stop data.",e);
		}
		
		trimToSize();
	}
	
	ArrayList<jStop> mCache;
	int ctl, cth, cgl, cgh;
	public ArrayList<jStop> getStopsInView(GeoPoint center, int lat_span, int lon_span)
	{
		int lat_low = center.getLatitudeE6() - (lat_span/2);
		int lat_high = center.getLatitudeE6() + (lat_span/2);
		int lon_low = center.getLongitudeE6() - (lon_span/2);
		int lon_high = center.getLongitudeE6() + (lon_span/2);
		
		if( ctl == lat_low && cth == lat_high &&
			cgl == lon_low && cgh == lon_high && mCache != null )
			return mCache;
		
		if( mCache == null )
			mCache = new ArrayList<jStop>();
		mCache.clear();
		
		int a = 0, c = mByLoc.size();
		while( a < c )
		{
			int b = (a+c)/2;
			jStop j = mByLoc.get(b);
			if( j.getPoint().getLatitudeE6() < lat_low ) a = b+1;
			else c = b;
		}
		
		int lat, lon;
		jStop j; c = mByLoc.size();
		while( a < c )
		{
			j = mByLoc.get(a); a++;
			lat = j.getPoint().getLatitudeE6();
			if( lat > lat_high ) break;
			
			lon = j.getPoint().getLongitudeE6();
			if( lat >= lat_low && lat <= lat_high &&
				lon >= lon_low && lon <= lon_high )
				mCache.add(j);
		}
		
		ctl = lat_low; cth = lat_high;
		cgl = lon_low; cgh = lon_high;
		return mCache;
	}
	
	public jStop findClosest(int latE6, int lonE6)
	{
		int a = 0, b, c = mByLoc.size()-1;
		jStop s;
		while( a < c )
		{
			b = (a+c)/2;
			s = mByLoc.get(b);
			if( latE6 < s.getLatE6() ) c = b-1;
			else if( latE6 > s.getLatE6() ) a = b+1;
			else a = c = b;
		}
		
		s = mByLoc.get(a); b = c = a;
		long d1 = latE6 - s.getLatE6();
		long d2 = lonE6 - s.getLonE6();
		long d1sq = d1*d1;
		long min_dist = d1sq + d2*d2;
		long d;
		int min_index = a;
		
		do {
			c++; if( c >= mByLoc.size() ) break;
			s = mByLoc.get(c);
			d1 = latE6 - s.getLatE6();
			d2 = lonE6 - s.getLonE6();
			d1sq = d1*d1;
			d = d1sq + d2*d2;
			if( d < min_dist )
			{
				min_index = c;
				min_dist = d;
			}
		} while( d1sq < min_dist );
		
		do {
			a--; if( a < 0 ) break;
			s = mByLoc.get(a);
			d1 = latE6 - s.getLatE6();
			d2 = lonE6 - s.getLonE6();
			d1sq = d1*d1;
			d = d1sq + d2*d2;
			if( d < min_dist )
			{
				min_index = a;
				min_dist = d;
			}
		} while( d1sq < min_dist );
		
		return mByLoc.get(min_index);
	}
	
	/**
	 * ListAdapter Interface Items
	 * 
	 */
	
	static LayoutInflater static_mInflater;
	static LayoutInflater getLayoutInflater(Context c)
	{
		if( static_mInflater == null )
			static_mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return static_mInflater;
	}
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
		jStop s = mById.get(p);
		LayoutInflater inflater = getLayoutInflater(parent.getContext());
		
		if(cView == null)
			cView = inflater.inflate(R.layout.row_stopdata,null);
		
		TextView rNum = (TextView) cView.findViewById(R.id.rsdStopNum);
		TextView rTcnt = (TextView) cView.findViewById(R.id.rsdTripCount);
		TextView rName = (TextView) cView.findViewById(R.id.rsdName);
		TextView rCoords = (TextView) cView.findViewById(R.id.rsdCoords);
		rNum.setText(Integer.toString(s.mId));
		rTcnt.setText(s.mRoutes.size() + " routes.");
		rName.setText(s.getTitle());
		rCoords.setText(((float)s.getPoint().getLatitudeE6())/1000000 + ", "
				+ ((float)s.getPoint().getLongitudeE6())/1000000 );
		
		return cView;
	}
	public int getViewTypeCount() { return 1; }
	public boolean hasStableIds() { return true; }
	public boolean isEmpty() { return mById.size() == 0; }
	public void registerDataSetObserver(DataSetObserver o) {}
	public void unregisterDataSetObserver(DataSetObserver o) {}
}
