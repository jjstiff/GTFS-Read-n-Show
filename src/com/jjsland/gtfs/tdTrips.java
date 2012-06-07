package com.jjsland.gtfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;

public class tdTrips {
	public final static String ASSET_FILE = "gtfs_vta/trips.txt";
	
	public final static String C_COUNT = "_COUNT";
	public final static String C_BLOCK = "block_id";
	public final static String C_ROUTE = "route_id";
	public final static String C_DIR = "direction";
	public final static String C_NAME = "trip_headsign";
	public final static String C_SERV_ID = "service_id";
	public final static String C_TRIP_ID = "trip_id";
	
	final static int I_COUNT = 1;
	final static int I_BLOCK = 2;
	final static int I_ROUTE = 3;
	final static int I_DIR = 4;
	final static int I_NAME = 5;
	final static int I_SERV_ID = 6;
	final static int I_TRIP_ID = 7;
	final static int I_UNKNOWN = 8;
	
	ArrayList<jTrip> mById; // TODO: Clear after stop-time data loaded
	
	public tdTrips(tdDownloader d, tdRoutes routes, tdCalendar cal) throws Exception
	{
		mById = new ArrayList<jTrip>(4000);
		readData(d,routes,cal);
	}
	
	public void trimToSize()
	{
		mById.trimToSize(); 
		Iterator<jTrip> i = mById.iterator();
		while(i.hasNext())
		{
			i.next().trimToSize();
		}
	}
	public void forgetTripsById()
	{ 
		mById.clear();
		mById.trimToSize();
	}
	
	public String toString()
	{
		if( mById == null || mById.size() <= 0 ) return "Trips contain no data.";
		return "Trips contain " + mById.size() + " entries.";
	}
	
	public int getSize()
	{
		return (mById == null) ? 0 : mById.size();
	}
	
	int[] parseHead(String line)
	{
		String[] d = line.split(",");
		int size = d.length;
		int[] h = new int[size];
		for( int i = 0; i < size; i++ )
		{
			d[i] = GTFSLoader.removeQuotes(d[i]);
			if( d[i].compareTo(C_BLOCK) == 0 ) h[i] = I_BLOCK;
			else if( d[i].compareTo(C_ROUTE) == 0 ) h[i] = I_ROUTE;
			else if( d[i].compareTo(C_DIR) == 0 ) h[i] = I_DIR;
			else if( d[i].compareTo(C_NAME) == 0 ) h[i] = I_NAME;
			else if( d[i].compareTo(C_SERV_ID) == 0 ) h[i] = I_SERV_ID;
			else if( d[i].compareTo(C_TRIP_ID) == 0 ) h[i] = I_TRIP_ID;
			else h[i] = I_UNKNOWN;
		}
		return h;
	}
	
	jTrip parseTrip(int[] head, String line, tdRoutes routes, tdCalendar cal)
	{
		String[] d = line.split(",");
		int size = (head.length<d.length) ? head.length : d.length;
		
		int route = 0;
		String name = null;
		int serv = 0;
		int trip = 0;
		
		try {
			for( int i = 0; i < size; i++ )
			{
				d[i] = GTFSLoader.removeQuotes(d[i]);
				switch(head[i]) {
				case I_ROUTE:		route = Integer.parseInt(d[i]); break;
				case I_NAME:		name = d[i]; break;
				case I_SERV_ID:		serv = Integer.parseInt(d[i]); break;
				case I_TRIP_ID:		trip = Integer.parseInt(d[i]); break;
				}
			}
		}
		catch( NumberFormatException e )
		{
			Log.e("tdTrips.parseTrip","Number format exception on \""+line+"\".");
			return null;
		}

		jCalendar c = cal.getServiceIfSelected(serv);
		if( c == null ) return null; // The Date is not selected.
		
		jRoute r = routes.findByID(route);
		if( r == null )
		{
			Log.e("tdTrips.parseTrip","Route "+route+" not found, not adding trip "+trip+ ".");
			return null;
		}
		jTrip out = new jTrip(trip,c,r);
		r.addTripInSeqById(name,out);
		return out;
	}

	void addById(jTrip t)
	{
		if( mById == null ) { mById = new ArrayList<jTrip>(4000); }
		
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
	jTrip findById(int id)
	{
		int a = 0, c = mById.size()-1;
		int b, v;
		jTrip t;
		if( cache >= 0 )
		{
			t = mById.get(cache);
			v = t.compareId(id);
			if( v == 0 ) return t;
			else if( v > 0 ) c = cache;
			else a = cache+1;
		}
		while( a < c )
		{
			b = (a+c)/2;
			t = mById.get(b);
			v = t.compareId(id);
			if( v == 0 ) { cache = b; return t; }
			else if( v > 0 ) c = b;
			else a = b+1;
		}
		return null;
	}
	
	public void readData(tdDownloader d, tdRoutes routes, tdCalendar cal)
		throws IOException
	{ 
		jZipData z = d.getTrips();
		InputStreamReader ir = new InputStreamReader(z.mStream);
		BufferedReader r = new BufferedReader(ir,256);
		String s = r.readLine(); // First line contains the field names
			
		int[] head = parseHead(s);
			
		while( (s = r.readLine()) != null )
		{
			jTrip t = parseTrip(head,s,routes,cal);
				if( t != null )
			{
				addById(t);
			}
		}
		z.mStream.close();
		
		trimToSize();
	}
}
