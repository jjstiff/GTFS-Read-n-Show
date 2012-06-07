package com.jjsland.gtfs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;

public class tdCalendar
{
	public final static String ASSET_FILE = "gtfs_vta/calendar.txt";

	public final static String C_ID = "service_id";
	public final static String C_START = "start_date";
	public final static String C_END = "end_date";
	public final static String C_MON = "monday";
	public final static String C_TUE = "tuesday";
	public final static String C_WED = "wednesday";
	public final static String C_THU = "thursday";
	public final static String C_FRI = "friday";
	public final static String C_SAT = "saturday";
	public final static String C_SUN = "sunday";

	final static int I_ID = 1;
	final static int I_START = 2;
	final static int I_END = 3;
	final static int I_MON = 4;
	final static int I_TUE = 5;
	final static int I_WED = 6;
	final static int I_THU = 7;
	final static int I_FRI = 8;
	final static int I_SAT = 9;
	final static int I_SUN = 10;
	final static int I_UNKNOWN = 11;
	
	ArrayList<jCalendar> mByValue;
	HashMap<Integer,jCalendar> mSelected;
	
	HashMap<Integer,jCalendar> mByServiceId;  // TODO: Clear after trip data loaded
	HashMap<Integer,Integer> mServiceIdMap;  // TODO: Clear after trip data loaded
	ArrayList<Calendar> mStartDates, mEndDates;  // TODO: Clear after trip data loaded
	int mEntryCount;
	
	public tdCalendar(tdDownloader d) throws Exception
	{
		readData(d);
	}
	
	public void trimToSize()
	{
		if( mByValue != null ) mByValue.trimToSize();
		if( mStartDates != null ) mStartDates.trimToSize();
		if( mEndDates != null ) mEndDates.trimToSize();
	}
	public void forgetServiceIdMap()
	{
		if( mByValue != null ) mByValue.trimToSize();
		if( mByServiceId != null ) mByServiceId.clear(); mByServiceId = null;
		if( mServiceIdMap != null ) mServiceIdMap.clear(); mServiceIdMap = null;
		if( mStartDates != null ) mStartDates.clear(); mStartDates = null;
		if( mEndDates != null ) mEndDates.clear(); mEndDates = null;
	}
	
	public String toString()
	{
		if( mByValue == null ) return "Calendar contains no service dates.";
		String out = "";
		if( mStartDates != null && mEndDates != null )
		{
			out += "Calendar has " + mStartDates.size() + " date ranges.\n";
			for( int i = 0; i < mStartDates.size(); i++)
				out += (i+1) + ": From " + jCalendar.dateToString(mStartDates.get(i))
							+ " to " + jCalendar.dateToString(mEndDates.get(i)) + ".\n";
			
		}
		out += "Calendar has " + mEntryCount + " entries with " + mByValue.size() + " unique.\n";
		
		out += "Using only:\n";
		Iterator<jCalendar> i = mSelected.values().iterator();
		while( i.hasNext() )
		{
			out += i.next().toString() + "\n";
		}
		return out;
	}
	
	public int getSize()
	{
		return (mByValue == null) ? 0 : mByValue.size();
	}
	
	public void clearMemory()
	{
	    if( mByValue != null ) { mByValue.clear(); mByValue = null; }
	}
	
	static int[] getHead(String line)
	{
		String[] d = line.split(",");
		int size = d.length;
		int[] h = new int[size];
		for( int i = 0; i < size; i++ )
		{
			d[i] = GTFSLoader.removeQuotes(d[i]);
			if( d[i].compareTo(C_ID) == 0 ) h[i] = I_ID;
			else if( d[i].compareTo(C_START) == 0 ) h[i] = I_START;
			else if( d[i].compareTo(C_END) == 0 ) h[i] = I_END;
			else if( d[i].compareTo(C_MON) == 0 ) h[i] = I_MON;
			else if( d[i].compareTo(C_TUE) == 0 ) h[i] = I_TUE;
			else if( d[i].compareTo(C_WED) == 0 ) h[i] = I_WED;
			else if( d[i].compareTo(C_THU) == 0 ) h[i] = I_THU;
			else if( d[i].compareTo(C_FRI) == 0 ) h[i] = I_FRI;
			else if( d[i].compareTo(C_SAT) == 0 ) h[i] = I_SAT;
			else if( d[i].compareTo(C_SUN) == 0 ) h[i] = I_SUN;
			else h[i] = I_UNKNOWN;
		}
		return h;
	}
	
	static Calendar parseDate(String s) throws NumberFormatException
	{
		int year = Integer.parseInt(s.substring(0,4));
		int month = Integer.parseInt(s.substring(4,6));
		int day = Integer.parseInt(s.substring(6,8));
		return new GregorianCalendar(year,month,day);
	}
	
	jCalendar getCal(int[] head, String line) throws Exception
	{
		String[] d = line.split(",");
		int size = (head.length<d.length) ? head.length : d.length;
		
		int id = 0;
		Calendar start = null;
		Calendar end = null;
		int mon=0, tue=0, wed=0, thu=0, fri=0, sat=0, sun=0;
		Exception error = null;
		
		for( int i = 0; i < size; i++ )
		{
		  d[i] = GTFSLoader.removeQuotes(d[i]);
		  try{
			switch(head[i]) {
			case I_ID:		id = Integer.parseInt(d[i]); break;
			case I_START:	start = parseDate(d[i]); break;
			case I_END:		end = parseDate(d[i]); break;
			case I_MON:		mon = Integer.parseInt(d[i]); break;
			case I_TUE:		tue = Integer.parseInt(d[i]); break;
			case I_WED:		wed = Integer.parseInt(d[i]); break;
			case I_THU:		thu = Integer.parseInt(d[i]); break;
			case I_FRI:		fri = Integer.parseInt(d[i]); break;
			case I_SAT:		sat = Integer.parseInt(d[i]); break;
			case I_SUN:		sun = Integer.parseInt(d[i]); break;
			}
		  }
		  catch(NumberFormatException e)
		  {
			  error = e;
		  } 
		}
		
		if( start == null || end == null )
		{
			throw new RuntimeException("Calendar entry is missing date range.", error);
		}
		
		return new jCalendar(id,start,end,sun,mon,tue,wed,thu,fri,sat);
	}

	public Integer getMappedId(Integer id) {
		if( mServiceIdMap == null ) return null;
		Integer v = mServiceIdMap.get(id);
		if( v == null ) v = id;
		return v;
	}
	
	public jCalendar getByServiceId(Integer id)
	{
		if( mByServiceId == null ) return null;
		Integer v = getMappedId(id);
		return mByServiceId.get(v);
	}
	
	public jCalendar getServiceIfSelected(Integer id)
	{
		return mSelected.get(getMappedId(id));
	}
	/**
	 * Call after tdTrips loads to map each trip to the correct calendar
	 * in order to conserve memory.
	 */
	public void clearIdMap() { mServiceIdMap.clear(); mServiceIdMap = null; }
	
	void addToServiceIdMap(Integer key,Integer value)
	{
		if( mServiceIdMap == null ) 
			mServiceIdMap = new HashMap<Integer,Integer>(20000);
		mServiceIdMap.put(key, value);
	}
	
	/**
	 * if the value does not exist, add the new jCalendar to mByValue and mById,
	 * if the value already exists, map the new id to the same value id.
	 * @param t jCalender to add.
	 */
	void addByValue(jCalendar t)
	{
		if( mByValue == null ) { mByValue = new ArrayList<jCalendar>(20); }
		if( mByServiceId == null ) { mByServiceId = new HashMap<Integer,jCalendar>(20); }
		
		int a = 0, c = mByValue.size();
		
		while( a < c )
		{
		   int b = (a+c)/2; 
		   jCalendar u = mByValue.get(b);
		   int v = t.compareValue(u);
		   if( v == 0 )
		   {
			   addToServiceIdMap(t.getServiceId(),u.getServiceId());
			   return;
		   }
		   else if( v < 0 ) { c = b; }
		   else { a = b+1; }
		}
		mByValue.add(c,t);
		mByServiceId.put(t.getServiceId(),t);
		addServiceDate(t);
	}
	
	void addServiceDate(jCalendar cal)
	{
		if( mStartDates == null) mStartDates = new ArrayList<Calendar>();
		if( mEndDates == null) mEndDates = new ArrayList<Calendar>();
		
		int a = 0, c = mStartDates.size();
		while(a < c)
		{
			int b = (a+c)/2;
			int v = cal.mStart.compareTo(mStartDates.get(b));
			if( v < 0 ) { c = b; }
			else if( v > 0 ) { a = b+1; }
			else {
				v = mEndDates.get(b).compareTo(cal.mEnd);
				if( v < 0 ) { c = b; }
				else if( v > 0 ) { a = b+1; }
				else return;
			}
		}
		mStartDates.add(c,cal.mStart);
		mEndDates.add(c,cal.mEnd);
	}
	
	public void readData(tdDownloader d) throws RuntimeException
	{
		try {
			mEntryCount = 0;
			jZipData z = d.getCalendar();
			InputStreamReader ir = new InputStreamReader(z.mStream);
			BufferedReader r = new BufferedReader(ir,256);
			String s = r.readLine(); // First line contains the field names
			int[] head = getHead(s);
			
			while( (s = r.readLine()) != null )
			{
				jCalendar t = getCal(head,s);
				addByValue(t);
				mEntryCount++;
			}
			z.mStream.close();
		
			chooseSelected();
			trimToSize();
		}
		catch(Exception e)
		{
			throw new RuntimeException("Error reading Calendar Data.\n", e);
		}
	}
	
	void chooseSelected()
	{	
		Calendar c = new GregorianCalendar(), d;
		int closest = -1;
		int closest_val = -1;
		for( int i = 0; i < mStartDates.size(); i++ )
		{
			int v = dateCompare(c,mStartDates.get(i),mEndDates.get(i));
			if( v < 0 ) v = -v;
			if( closest < 0 || v < closest_val )
			{
				closest = i;
				closest_val = v;
			}
		}
		
		if( mSelected == null )
			mSelected = new HashMap<Integer,jCalendar>(10);
		mSelected.clear();
		Iterator<jCalendar> j = mByValue.iterator();
		c = mStartDates.get(closest);
		d = mEndDates.get(closest);
		while( j.hasNext() )
		{
			jCalendar k = j.next();
			if( k.mStart.equals(c) && k.mEnd.equals(d) )
			{
				mSelected.put(k.getServiceId(),k);
			}
		}
	}
	
	/**
	 * @return How many days before start (positive) or after end (negative),
	 * zero if within date range, inclusive.
	 */
	static final long MILLIS_PER_DAY = (1000*60*60*24);
	static int dateCompare(Calendar cmp, Calendar start, Calendar end)
	{
		long cT = cmp.getTimeInMillis();
		long sT = start.getTimeInMillis();
		long eT = end.getTimeInMillis();
		
		if( cT < sT )
			return 1 + (int)((sT-cT)/MILLIS_PER_DAY);
		
		else if( cT > eT + MILLIS_PER_DAY )
			return -1 + (int)((eT+MILLIS_PER_DAY-cT)/MILLIS_PER_DAY);
		
		else 
			return 0;
	}
}
