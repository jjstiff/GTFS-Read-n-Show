package com.jjsland.gtfs;

import java.util.Calendar;

public class jCalendar
{
	int mServiceId;
	Calendar mStart, mEnd;
	int mDays;
	
	static final int TODAY = 0x80;
	static final int TOMORROW = 0x100;
	static final int SUN = 0x40;
	static final int MON = 0x20;
	static final int TUE = 0x10;
	static final int WED = 0x08;
	static final int THU = 0x04;
	static final int FRI = 0x02;
	static final int SAT = 0x01;
	
	static int msWhenToShow = TODAY;
	static public int getWhenToShow() { return msWhenToShow; }
	static public boolean showTrip(jTrip t)
	{
		if( t == null || t.mServ == null ) return false;
		return t.mServ.matchesWhen(msWhenToShow);
	}
	static public void setWhenToShow(int when) { msWhenToShow = when; }
	
	static public boolean isShowing_all()
	{
		return (msWhenToShow^(SUN|MON|TUE|WED|THU|FRI|SAT)) == 0;
	} 
	static public boolean isShowing_sun_only()
	{
		return (msWhenToShow^SUN) == 0
				|| (((msWhenToShow^TODAY) == 0) && isToday(SUN))
				|| (((msWhenToShow^TOMORROW) == 0) && isTomorrow(SUN));
	}
	static public boolean isShowing_sat_only()
	{
		return (msWhenToShow^SAT) == 0
				|| (((msWhenToShow^TODAY) == 0) && isToday(SAT))
				|| (((msWhenToShow^TOMORROW) == 0) && isTomorrow(SAT));
	}
	static public boolean isShowing_mtwrf_only()
	{
		int mtwrf = MON|TUE|WED|THU|FRI;
		return (msWhenToShow^mtwrf) == 0
				|| (((msWhenToShow^TODAY) == 0) && isToday(mtwrf))
				|| (((msWhenToShow^TOMORROW) == 0) && isTomorrow(mtwrf));
	}
	static public boolean isShowing_today()
	{
		return (msWhenToShow^TODAY) == 0 || isToday(msWhenToShow);
	}
	static public boolean isShowing_tomorrow()
	{
		return (msWhenToShow^TOMORROW) == 0 || isTomorrow(msWhenToShow);
	}
	
	public jCalendar(int id, Calendar start, Calendar end, int sun, int mon, int tue, int wed, int thu, int fri, int sat)
	{
		mServiceId = id;
		mStart = start; 
		mEnd = end;
		mDays = 0;
		if( sun > 0 ) mDays |= SUN;
		if( mon > 0 ) mDays |= MON;
		if( tue > 0 ) mDays |= TUE;
		if( wed > 0 ) mDays |= WED;
		if( thu > 0 ) mDays |= THU;
		if( fri > 0 ) mDays |= FRI;
		if( sat > 0 ) mDays |= SAT;
	}
	
	public String toString()
	{
		String out = dateToString(mStart) + " to " + dateToString(mEnd) + " ";
		out += ((mDays & SUN) > 0) ? "U" : "_";
		out += ((mDays & MON) > 0) ? "M" : "_";
		out += ((mDays & TUE) > 0) ? "T" : "_";
		out += ((mDays & WED) > 0) ? "W" : "_";
		out += ((mDays & THU) > 0) ? "R" : "_";
		out += ((mDays & FRI) > 0) ? "F" : "_";
		out += ((mDays & SAT) > 0) ? "S" : "_";
		
		return out;
	}

	public static String dateToString(Calendar c)
	{
		return c.get(Calendar.YEAR) + "-"
				+ c.get(Calendar.MONTH) + "-"
				+ c.get(Calendar.DAY_OF_MONTH);
	}
	
	public int compareServiceId(jCalendar c) { return mServiceId-c.mServiceId; }
	public int compareServiceId(int id) { return mServiceId-id; }
	public int getServiceId() { return mServiceId; }
	
	/**
	 * All these checks are here because of a null pointer exception happening
	 * at the line mStart.compareTo(c.mStart). I recieved the error report from
	 * android market but cannot see exactly why there error may have occured.
	 * @param c
	 * @return
	 */
	public int compareValue(jCalendar c)
	{
		if( c == null ) return 1;
		if( mStart == null && c.mStart == null ) return 0;
		if( mStart == null ) return -1;
		if( c.mStart == null ) return 1;
		int out = mStart.compareTo(c.mStart);
		if( out != 0 ) return out;
		if( mEnd == null && c.mEnd == null ) return 0;
		if( mEnd == null ) return -1;
		if( c.mEnd == null ) return 1;
		out = mEnd.compareTo(c.mEnd);
		if( out != 0 ) return out;
		return mDays - c.mDays;
	}
	
	private boolean isToday() { return isToday(mDays); }
	public static boolean isToday(int when)
	{
		int dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		switch( dow )
		{
		case Calendar.SUNDAY: return (when & SUN) > 0;
		case Calendar.MONDAY: return (when & MON) > 0;
		case Calendar.TUESDAY: return (when & TUE) > 0;
		case Calendar.WEDNESDAY: return (when & WED) > 0;
		case Calendar.THURSDAY: return (when & THU) > 0;
		case Calendar.FRIDAY: return (when & FRI) > 0;
		case Calendar.SATURDAY: return (when & SAT) > 0;
		default: return false;
		}
	}
	
	public boolean matchesWhen(int when)
	{
		if( (mDays & when) > 0 ) return true;
		if( (TODAY & when) > 0 && isToday() ) return true;
		if( (TOMORROW & when) > 0 && isTomorrow() ) return true;
		return false;
	}
	
	private boolean isTomorrow() { return isTomorrow(mDays); }
	public static boolean isTomorrow(int when)
	{
		int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		
		switch( today )
		{
		case Calendar.SUNDAY: return (when & MON) > 0;
		case Calendar.MONDAY: return (when & TUE) > 0;
		case Calendar.TUESDAY: return (when & WED) > 0;
		case Calendar.WEDNESDAY: return (when & THU) > 0;
		case Calendar.THURSDAY: return (when & FRI) > 0;
		case Calendar.FRIDAY: return (when & SAT) > 0;
		case Calendar.SATURDAY: return (when & SUN) > 0;
		default: return false;
		}
	}
	
	static public String whenToString(int when)
	{
		boolean u = (when & SUN) > 0;
		boolean m = (when & MON) > 0;
		boolean t = (when & TUE) > 0;
		boolean w = (when & WED) > 0;
		boolean r = (when & THU) > 0;
		boolean f = (when & FRI) > 0;
		boolean s = (when & SAT) > 0;
		boolean h = (when & TODAY) > 0;
		boolean n = (when & TOMORROW) > 0;
		boolean mtwrf = ( m || t || w || r || f);
		
		if( h ) return "TODAY";
		if( n ) return "TOM";
		if( u && mtwrf && s ) return "ALL";
		if( u ) return "SUN";
		if( mtwrf ) return "MTWRF";
		if( s ) return "SAT";
		return "none";
	}
	
}
