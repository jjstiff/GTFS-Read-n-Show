package com.jjsland.gtfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.util.Log;


public class tdStopTimes {
	public final static String ASSET_FILE = "gtfs_vta/stop_times.txt";
	
	public final static String C_COUNT = "_COUNT";
	public final static String C_ID = "_ID";
	public final static String C_TRIP = "trip_id";
	public final static String C_ATIME = "arrival_time";
	public final static String C_DTIME = "departure_time";
	public final static String C_STOP_ID = "stop_id";
	public final static String C_STOP_SEQU = "stop_sequence";
	public final static String C_STOP_NAME = "stop_headsign";
	public final static String C_PICKUP = "pickup_type";
	public final static String C_DROPOFF = "drop_off_type";
	public final static String C_DIST = "shape_dist_traveled";

	final static int I_COUNT = 1;
	final static int I_ID = 2;
	final static int I_TRIP = 3;
	final static int I_ATIME = 4;
	final static int I_DTIME = 5;
	final static int I_STOP_ID = 6;
	final static int I_STOP_SEQU = 7;
	final static int I_STOP_NAME = 8; // not used
	final static int I_PICKUP = 9; // not used
	final static int I_DROPOFF = 10; // not used
	final static int I_DIST = 11;  // not used
	final static int I_UNKNOWN = 12; 

	private tdStopTimes() { /* static only class */ }

	static int[] parseHead(String line)
	{
		String[] d = line.split(",");
		int size = d.length;
		int[] h = new int[size];
		for( int i = 0; i < size; i++ )
		{
			d[i] = GTFSLoader.removeQuotes(d[i]);
			if( d[i].compareTo(C_TRIP) == 0 ) h[i] = I_TRIP;
			else if( d[i].compareTo(C_ATIME) == 0 ) h[i] = I_ATIME;
			else if( d[i].compareTo(C_DTIME) == 0 ) h[i] = I_DTIME;
			else if( d[i].compareTo(C_STOP_ID) == 0 ) h[i] = I_STOP_ID;
			else if( d[i].compareTo(C_STOP_SEQU) == 0 ) h[i] = I_STOP_SEQU;
			else if( d[i].compareTo(C_STOP_NAME) == 0 ) h[i] = I_STOP_NAME;
			else if( d[i].compareTo(C_PICKUP) == 0 ) h[i] = I_PICKUP;
			else if( d[i].compareTo(C_DROPOFF) == 0 ) h[i] = I_DROPOFF;
			else if( d[i].compareTo(C_DIST) == 0 ) h[i] = I_DIST;
			else h[i] = I_UNKNOWN;
		}
		return h;
	}
	
	public static int getNow()
	{
		Calendar g = new GregorianCalendar();
		int out = g.get(Calendar.HOUR_OF_DAY);
		out *= 60;
		out += g.get(Calendar.MINUTE);
		out *= 60;
		out += g.get(Calendar.SECOND);
		return out;
	}
	
	static boolean ms24hour = false;
	public static String timeToString(Integer time)
	{
		if(time == null) return "--:--x";
		int min = time/60;
		int hour = min/60;
		min %= 60;
		String out;
		if( min < 10 ) out = ":0" + min; else out = ":" + min;
		if( ms24hour )
		{
			if( hour < 10 ) out = "0" + hour + out;
			else if( hour < 24 ) out = hour + out;
			else if( hour < 34 ) out = "0" + (hour-24) + out;
			else out = (hour-24) + out;
		}
		else
		{
			if( hour > 24 ) out = (hour-24) + out + "a";
			else if( hour > 12 ) out = (hour-12) + out + "p";
			else out = hour + out + "a";
		}
		return out;
	}

	/**
	 * 
	 * @return number of seconds from midnight;
	 * can be greater than 24*60*60 if the trip starts before midnight
	 * and runs until after midnight.
	 * returns -1 if t is not a time.
	 */
	static int parseTime(String t)
	{
		String[] d = t.split(":");
		if( d.length != 3 ) return -1;
		return Integer.parseInt(d[0])*60*60
				+ Integer.parseInt(d[1])*60
				+ Integer.parseInt(d[0]);
	}
	
	static boolean parseStopTime(int[] head, String line, tdStops stops, tdTrips trips)
	{
		int trip_id=0,stop_id=0,stop_seq=0,atime=0;
		
		String[] d = line.split(",");
		int size = (head.length < d.length ? head.length : d.length);
		try {
			for( int i = 0; i < size; i++ )
			{
				d[i] = GTFSLoader.removeQuotes(d[i]);
				switch(head[i]) {
				case I_TRIP:		trip_id = Integer.parseInt(d[i]); break;
				case I_ATIME:		atime = parseTime(d[i]); break;
				case I_STOP_ID:		stop_id = Integer.parseInt(d[i]); break;
				case I_STOP_SEQU:	stop_seq = Integer.parseInt(d[i]); break;
				}
			}
		}
		catch( NumberFormatException e )
		{
			Log.e("tdStopTimes.parseStopTime","Error reading line: " + line);
			return false;
		}
		
		jTrip t = trips.findById(trip_id);
		if( t == null ) return false;
		
		jStop p = stops.findById(stop_id);
		if( p == null ) return false;
		
		t.addStopTime(p, stop_seq, atime);
		p.addRoute(t.getRoute());
		return true;
	}
	
	static public int readData(tdDownloader d, tdStops stops, tdTrips trips, GTFSLoader.LoadVTATask task)
			throws IOException
	{
		int count = 0, lines = 0;
		jZipData z = d.getStopTimes();
		long dataSize = z.mEntry.getSize();
		long readSize = 0;
		InputStreamReader ir = new InputStreamReader(z.mStream);
		BufferedReader r = new BufferedReader(ir,256);
		String s = r.readLine(); // First line contains the field names
		readSize += s.length()+2;
		int[] head = parseHead(s);
		while( (s = r.readLine()) != null )
		{
			readSize += s.length()+2;
			lines++;
			if( parseStopTime(head,s,stops,trips) )
				count++;
			if( lines % 1000 == 0 )
				task.publishStopTimeProgress(count,readSize,dataSize);
		}
		z.mStream.close();
		
		return count;
	}
}
