package com.jjsland.gtfs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class aStartup extends Activity implements GTFSLoadListener {

	GTFSLoader mLoader;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gtfsloader);
        mLoader = gtfsApp.getFirstLoader();
        mLoader.addGTFSLoadListener(this);
        enableButtons();
    }
    
    static final int DIALOG_ERROR = 0;
    Exception mError;
    public void doErrorDialog(Exception e)
    {
    	mError = e;
    	showDialog(DIALOG_ERROR);
    }
    
    public Dialog onCreateDialog(int id)
    {
    	if( id == DIALOG_ERROR )
    	{
    		AlertDialog.Builder b = new AlertDialog.Builder(this);
    		String mess = mError.getMessage();
    		Throwable t = mError.getCause();
    		if(t != null)
    		{
    			mess += "\n" + t.getMessage();
    			t = t.getCause();
    			if( t != null )
    				mess += "\n" + t.getMessage();
    		}
    		b.setMessage(mess);
    		b.setCancelable(false);
    		b.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                doRetry();
    	           }
    	    	});
    		b.setNeutralButton("Re-Download", new DialogInterface.OnClickListener() {
 	           		public void onClick(DialogInterface dialog, int id) {
 	           			doRetryAndRedownload();
 	           		}
    		   	});
    		b = b.setNegativeButton("Report", new DialogInterface.OnClickListener() {
 	           public void onClick(DialogInterface dialog, int id) {
 	                throw new RuntimeException(mError);
 	           }
 	        });
    		AlertDialog a = b.create();
    		return a;
    	}
    	return null;
    }
    
    public void doRetry()
    {
    	mLoader.clearMemory();
    	enableButtons();
    	mLoader.load();
    }

    public void doRetryAndRedownload()
    {
    	mLoader.clearMemory();
    	mLoader.getDownloader().deleteDataFile();
    	enableButtons();
    	mLoader.load();
    }
    
    protected void enableButtons()
    {
    	boolean stops = (mLoader.getStep() >= GTFSLoader.STEP_STOPS); // Allow showing map if not downloading.
    	boolean complete = (mLoader.getStep() == GTFSLoader.STEP_COMPLETE); // Wait to show schedules until all data is loaded.
    	boolean downloaded = (mLoader.getStep() >= GTFSLoader.STEP_DOWNLOAD); // Allow entering destination once downloading is complete.
        Button b = (Button) findViewById(R.id.bShowMap);
        b.setEnabled(stops);
        b = (Button) findViewById(R.id.bShowSchedule);
        b.setEnabled(complete);
        b = (Button) findViewById(R.id.bEnterDest);
        b.setEnabled(false);//downloaded); // Disabled for now...
        b.setVisibility(View.GONE); // Hide it too...
        b = (Button) findViewById(R.id.bShowStops);
        b.setEnabled(stops);
        b.setVisibility(View.GONE); // Hide this for now...
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	// Do nothing
    }
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	mLoader.removeGTFSLoadListener(this);
    	mLoader = null;
    }
    
    public void onGTFSLoadEvent(int step, long time)
    {
    	TextView tv = (TextView) findViewById(R.id.tvMessage);
    	tv.append(aManageData.getLoadEventMessage(mLoader,step,time));
    	final ScrollView sv = (ScrollView) findViewById(R.id.svMessage);
    	sv.post(new Runnable() { @Override public void run() { 
    		sv.fullScroll(ScrollView.FOCUS_DOWN); } });
    	enableButtons();
    }
    public void onGTFSLoadStopTimeUpdate(int count, long bytesRead, long bytesTotal)
    {
    	TextView tv = (TextView) findViewById(R.id.tvMessage);
    	tv.append(aManageData.getStopTimeCountMessage(count, bytesRead, bytesTotal));
    	final ScrollView sv = (ScrollView) findViewById(R.id.svMessage);
    	sv.post(new Runnable() { @Override public void run() { 
    		sv.fullScroll(ScrollView.FOCUS_DOWN); } });
    }
    
    public void doShowMap(View v)
    {
    	Intent i = new Intent(this,aShowMap.class);
    	startActivity(i);
    }
    
    public void doShowSchedule(View v)
    {
    	Intent i = new Intent(this,aShowSchedule.class);
    	startActivity(i);
    }
    
    public void doEnterDest(View v)
    {
    	Intent i = new Intent(this,aEnterDest.class);
    	startActivity(i);
    }
    
    public void doDataManage(View v)
    {
    	//Intent i = new Intent(this,aManageData.class);
    	Intent i = new Intent(this,aManageData.class);
    	startActivity(i);
    }
    
    public void doAboutBox(View v)
    {
    	Intent i = new Intent(this,aAboutBox.class);
    	startActivity(i);
    }
    
    public void doShowStops(View v)
    {
    	Intent i = new Intent(this,aShowRoute.class);
    	//Intent i = new Intent(this,aShowStops.class);
    	startActivity(i);
    }
}
