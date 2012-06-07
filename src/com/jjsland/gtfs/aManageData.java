package com.jjsland.gtfs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class aManageData extends Activity implements GTFSLoadListener
{	
	GTFSLoader mLoader;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.managedata);
        mLoader = gtfsApp.getFirstLoader();
        if( mLoader != null ) mLoader.addGTFSLoadListener(this);
        enableButtons();
    }
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	if( mLoader != null ) mLoader.removeGTFSLoadListener(this);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	// Do nothing
    }
    
    @Override
    public void onResume() {
    	super.onPause();
    	enableButtons();
    }
    
    protected void enableButtons()
    {
    	boolean rdf = (mLoader.getStep() == GTFSLoader.STEP_COMPLETE);
    	boolean cm = rdf;
    	boolean rd = rdf;
    	if( rdf )
    	{
    		tdDownloader d = mLoader.getDownloader();
    		rdf = (d!=null && d.hasDownloadedData());
    	}
    	
    	if( cm )
    		cm = (mLoader.getDownloader() != null);
    	
        Button b = (Button) findViewById(R.id.bRemoveDataFile);
        b.setEnabled(rdf);
        b = (Button) findViewById(R.id.bClearMemory);
        b.setEnabled(cm);
        b = (Button) findViewById(R.id.bReloadData);
        b.setEnabled(rd);
    }
    
    public void onGTFSLoadEvent(int step, long time)
    {
    	TextView tv = (TextView) findViewById(R.id.tvMessage);
    	tv.append(getLoadEventMessage(mLoader,step,time));
    	final ScrollView sv = (ScrollView) findViewById(R.id.svMessage);
    	sv.post(new Runnable() { @Override public void run() { 
    		sv.fullScroll(ScrollView.FOCUS_DOWN); } });
    	if(step == GTFSLoader.STEP_COMPLETE)
    	{
            enableButtons();
    	}
    }
    
    public void onGTFSLoadStopTimeUpdate(int count, long bytesRead, long bytesTotal)
    {
    	TextView tv = (TextView) findViewById(R.id.tvMessage);
    	tv.append(getStopTimeCountMessage(count,bytesRead,bytesTotal));
    	final ScrollView sv = (ScrollView) findViewById(R.id.svMessage);
    	sv.post(new Runnable() { @Override public void run() { 
    		sv.fullScroll(ScrollView.FOCUS_DOWN); } });
    }
    
    static String getLoadEventMessage(GTFSLoader l, int step, long time)
    {
    	String mess = "";
    	switch(step)
    	{
    	case GTFSLoader.STEP_START:
    		mess = "\n\nDownloading Data...";
    		break;
    	case GTFSLoader.STEP_DOWNLOAD:
    		mess = l.getDownloader().getZippedContentString();
    		mess += getTimeMessage(time);
    		mess += "\nLoading Stop Data...";
    		break;
    	case GTFSLoader.STEP_STOPS:
    		mess = "Stop Data contain " + l.getStops().getSize() + " stops.\n";
    		mess += getTimeMessage(time);
    		mess += "\nLoading Route Data...";
    		break;
    	case GTFSLoader.STEP_ROUTES:
    		mess = "Route Data contain " + l.getRoutes().getCount() + " routes.\n";
    		mess += getTimeMessage(time);
    		mess += "\nLoading Calendar Data...";
    		break;
    	case GTFSLoader.STEP_CAL:
    		mess = "Calendar Data contain " + l.getCal().getSize() + " unique entries.\n";
    		mess += l.getCal().toString();
    		mess += getTimeMessage(time);
    		mess += "\nLoading Trip Data...";
    		break;
    	case GTFSLoader.STEP_TRIPS:
    		mess = "Trip Data contain " + l.getTrips().getSize() + " trips.\n";
    		mess += getTimeMessage(time);
    		mess += "\nLoading Stop-Time Data...";
    		break;
    	case GTFSLoader.STEP_TIMES:
    		mess = "Stop-Time Data contain " + l.getStopTimeCount() + " entries.\n";
    		mess += getTimeMessage(time);
    		mess += "\nSorting Trips By Time...";
    		break;
    	case GTFSLoader.STEP_COMPLETE:
    		mess = getTimeMessage(time);
    		mess += "\n\nData Loading Complete.\n";
    		break;
    	default:
    		mess = "Ummm.... shouldn't be printing this...\n";
    		break;
    	}
    	return mess + "\n";
    }
    
    static long smSTCtime = -1;
    static String getStopTimeCountMessage(int count, long bytesRead, long bytesTotal)
    {
    	if( smSTCtime < 0 ) smSTCtime = System.nanoTime();
    	long t = System.nanoTime();
    	long secs = (t-smSTCtime)/1000000;
    	smSTCtime = t;
    	
    	String p = bytesTotal>0 ? ( ((int)(bytesRead*1000/bytesTotal))/10.0 + "%" ) : (bytesRead + "b");
    	return count+" entries, " + p + ", " + secs/1000.0f + " secs.\n";
    }
    
    static String getTimeMessage(long time)
    {
    	time /= 1000000;
    	return "Took " + ((float)time)/1000 + " seconds to complete.\n\n";
    }
    
    public void doRemoveDataFile(View v)
    {
    	mLoader.getDownloader().deleteDataFile();
    	enableButtons();
    }
    
    public void doClearMemory(View v)
    {
    	mLoader.clearMemory();
    	enableButtons();
    }
    
    public void doReloadData(View v)
    {
    	doClearMemory(v);
    	mLoader.load();
    	enableButtons();
    }
}
