package com.jjsland.gtfs;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.AsyncTask;

/**
 * 
 * @author jjstiff
 *
 */
public class GTFSLoader
{
	private String mName;
	private tdDownloader mDataFile;
	private tdCalendar mCalendar;
	private tdRoutes mRoutes;
    private tdStops mStops;
    private tdTrips mTrips;
    private int mStopTimeCount;
    private int mStep;
    private Exception mError;
    private ArrayList<GTFSLoadListener> mListeners = new ArrayList<GTFSLoadListener>();
    private long[] mEventTime = new long[STEP_COMPLETE+1];
    private LoadVTATask mTask;

    public static final int STEP_START = 0;
    public static final int STEP_DOWNLOAD = 1;
    public static final int STEP_STOPS = 2;
    public static final int STEP_ROUTES = 3;
    public static final int STEP_CAL = 4;
    public static final int STEP_TRIPS = 5;
    public static final int STEP_TIMES = 6;
    public static final int STEP_COMPLETE = 7;
    public int getStep() { return mStep; }
    
    public tdDownloader getDownloader() { return mDataFile; }
    public tdCalendar getCal() { return mCalendar; }
    public tdRoutes getRoutes() { return mRoutes; }
    public tdStops getStops() { return mStops; }
    public tdTrips getTrips() { return mTrips; }
    public int getStopTimeCount() { return mStopTimeCount; }
    
    public GTFSLoader(String name, String url, String save)
    {
    	mName = name;
    	mDataFile = new tdDownloader(url,save);
    	mListeners = new ArrayList<GTFSLoadListener>();
    	clearMemory();
    	load();
    }
    
    public void clearMemory()
    {
    	if( mCalendar != null) mCalendar.clearMemory(); mCalendar = null;
    	if( mCalendar != null) mCalendar.clearMemory(); mRoutes = null;
    	if( mCalendar != null) mCalendar.clearMemory(); mStops = null;
    	if( mCalendar != null) mCalendar.clearMemory(); mTrips = null;
    	mStopTimeCount = 0;
    	mStep = STEP_START;
    	mError = null;
    	mTask = null;
    }
    
    public void load()
    {
    	if( mTask != null )
    	{
    		mTask.cancel(true);
    		clearMemory();
    	}
    	mTask = new LoadVTATask();
    	mTask.execute();
    }
    
    public jRoute getRouteById(int route_id)
    {
    	return mRoutes == null ? null : mRoutes.findByID(route_id);
    }
    
    public void addGTFSLoadListener(GTFSLoadListener g)
    {
    	Iterator<GTFSLoadListener> i = mListeners.iterator();
    	while(i.hasNext()) { if(i.next() == g) return; }
    	
    	mListeners.add(g);
    	for( int s = STEP_START; s <= mStep; s++ )
    	{
    		g.onGTFSLoadEvent(s,mEventTime[s]);
    	}
    }
    
    public void removeGTFSLoadListener(GTFSLoadListener g)
    {
    	Iterator<GTFSLoadListener> i = mListeners.iterator();
    	while(i.hasNext()) { if(i.next() == g) i.remove(); }
    }
	
	public jBoundsE6 getBounds()
	{
		if( mStops != null )
			return mStops.getBounds();
		else
			return null;
	}
	
	/**
	 * When the gtfs data file is read, the data may be delimited with or without quotes.
	 * This method removes the quotes if they exist.
	 */
	public static final String removeQuotes(String q)
	{
		if( q.length() >= 2 && q.startsWith("\"") && q.endsWith("\"") )
			q = q.substring(1,q.length()-1);
		else if(  q.length() >= 2 && q.startsWith("\'") && q.endsWith("\'")  )
			q = q.substring(1,q.length()-1);
		return q;
	}
    
	/**
	 * After each step is complete, a message is sent to the listeners.
	 */
    private void doGTFSLoadEvent(int step, long time)
    {
    	mEventTime[step] = time;
    	Iterator<GTFSLoadListener> i = mListeners.iterator();
    	while(i.hasNext())
    	{
    		GTFSLoadListener l = i.next();
    		if( l != null ) l.onGTFSLoadEvent(step, time);
    	}
    }
    
    /**
     * During stop-time input, a message is periodically sent to the listeners.
     */
    private void doGTFSLoadStopTimeUpdate(int count, long bytesRead, long bytesTotal)
    {
    	Iterator<GTFSLoadListener> i = mListeners.iterator();
    	while(i.hasNext())
    	{
    		GTFSLoadListener l = i.next();
    		if( l != null ) l.onGTFSLoadStopTimeUpdate(count, bytesRead, bytesTotal);
    	}
    }
    
    protected class LoadVTATask extends AsyncTask<Void,Void,Void>
    {
    	long mTime;
    	boolean doingStopTimes;
    	int stCount;
    	long stBytesRead, stBytesTotal;
    	protected void onPreExecute() 
    	{
    		mTime = System.nanoTime();
    		doingStopTimes = false;
    		mError = null;
    	}
    	protected Void doInBackground(Void... voids)
    	{
    		try {
    			downloadData();
    			publishProgress();
    			readStops();
    			publishProgress();
    			readRoutes();
    			publishProgress();
    			readCalendar();
    			publishProgress();
    			readTrips();
    			mCalendar.forgetServiceIdMap();
    			publishProgress();
    			doingStopTimes = true;
    			readStopTimes(this);
    			doingStopTimes = false;
    			publishProgress();
    			// Cleanup...
				mTrips.forgetTripsById();
				mRoutes.trimToSize();
				//mRoutes.normalize();
				//mRoutes.combineDuplicateSequences();
				mRoutes.sortTripsByTime();
				mStep = STEP_COMPLETE;
				publishProgress();
				return null;
    		}
    		catch(Exception e)
    		{
    			mStep = STEP_START;
    			mError = e;
    			publishProgress();
    			return null;
    		}
    	}
    	public void publishStopTimeProgress(int count, long bytesRead, long bytesTotal)
    	{
    		stCount = count;
    		stBytesRead = bytesRead;
    		stBytesTotal = bytesTotal;
    		publishProgress();
    	}
    	protected void onPostExecute(Void v) {}
    	protected void onProgressUpdate(Void... v)
    	{
    		if( mError != null )
    		{
    		//	mAct.doErrorDialog(mError);
    		}
    		else if( doingStopTimes )
    		{
    			doGTFSLoadStopTimeUpdate(stCount,stBytesRead,stBytesTotal);
    		}  
    		else
    		{
    			long t = System.nanoTime();
    			doGTFSLoadEvent(mStep,t-mTime);
    			mTime = t;
    		}
    	}
    }
    
    private int downloadData() throws Exception
    {
    	mDataFile.downloadData();
    	mStep = STEP_DOWNLOAD;
    	return 0;
    }
    
    private int readCalendar() throws Exception
    {
    	mCalendar = new tdCalendar(mDataFile);
    	mStep = STEP_CAL;
    	return mCalendar.getSize();
    }
	
    private int readStops() throws Exception
	{
		mStops = new tdStops(mDataFile);
    	mStep = STEP_STOPS;
		return mStops.getSize();
	}

    private int readRoutes() throws Exception
	{
		mRoutes = new tdRoutes(mDataFile);
		mStep = STEP_ROUTES;
		return mRoutes.getCount();
	}
	
    private int readTrips() throws Exception
	{
		if(mRoutes == null || mCalendar == null) return 0;
		mTrips = new tdTrips(mDataFile,mRoutes,mCalendar);
		mStep = STEP_TRIPS;
		return mTrips.getSize();
	}
	
    private int readStopTimes(LoadVTATask t) throws Exception
	{
		if(mTrips == null || mStops == null) return 0;
    	mStopTimeCount = tdStopTimes.readData(mDataFile,mStops,mTrips,t);
    	mStep = STEP_TIMES;
    	return mStopTimeCount;
	}
}
