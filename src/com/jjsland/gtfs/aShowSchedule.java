package com.jjsland.gtfs;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class aShowSchedule extends ListActivity implements GTFSLoadListener
{
	GTFSLoader mLoader;
    
    boolean mRoutesLoaded, mTripsLoaded, mTimesLoaded;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_showschedule);

		LayoutInflater l = getLayoutInflater();
		View dayButtons = l.inflate(R.layout.sub_day_selector, null);
		ViewGroup g = (ViewGroup) findViewById(R.id.ssHeader);
		g.addView(dayButtons);
		
        mRoutesLoaded = mTripsLoaded = mTimesLoaded = false;
        mLoader = gtfsApp.getFirstLoader();
        mLoader.addGTFSLoadListener(this);
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
        refreshView();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	// Do nothing
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
        mLoader.removeGTFSLoadListener(this);
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int p, long id)
	{
		if(mTimesLoaded)
		{
			jRoute r = (jRoute) l.getAdapter().getItem(p);
	    	Intent i = new Intent(this,aShowRoute.class);
	    	i.putExtra(aShowRoute.ROUTE, r.mId);
	    	startActivity(i);
		}
		else
		{
			Toast t = Toast.makeText(getApplicationContext(),"Wait for Stop-Times to Load...",Toast.LENGTH_SHORT);
			t.show();
		}
	}
    
	public void onGTFSLoadEvent(int step, long time)
	{
		switch( step )
		{
		case GTFSLoader.STEP_TIMES: mTimesLoaded = true; break;
		case GTFSLoader.STEP_TRIPS: mTripsLoaded = true; break;
		case GTFSLoader.STEP_ROUTES: mRoutesLoaded = true; break;
		}
		refreshView();
	}
    public void onGTFSLoadStopTimeUpdate(int count, long bytesRead, long bytesTotal) {}
    
    private void refreshDayButtons()
    {
		Button b = ((Button)findViewById(R.id.bDayAll));
		if( b != null ) b.setEnabled(!jCalendar.isShowing_all());
		b = ((Button)findViewById(R.id.bDaySUN));
		if( b != null ) b.setEnabled(!jCalendar.isShowing_sun_only());
		b = ((Button)findViewById(R.id.bDaySAT));
		if( b != null ) b.setEnabled(!jCalendar.isShowing_sat_only());
		b = ((Button)findViewById(R.id.bDayMTWRF));
		if( b != null ) b.setEnabled(!jCalendar.isShowing_mtwrf_only());
		b = ((Button)findViewById(R.id.bDayToday));
		if( b != null ) b.setEnabled(!jCalendar.isShowing_today() || jCalendar.isShowing_all());
		b = ((Button)findViewById(R.id.bDayTomorrow));
		if( b != null ) b.setEnabled(!jCalendar.isShowing_tomorrow() || jCalendar.isShowing_all());

    }
    
    private void refreshView()
    {
    	refreshDayButtons();
    	setListAdapter(mLoader.getRoutes());
    }
    
    public void doDaySUN(View v)
    {
    	jCalendar.setWhenToShow(jCalendar.SUN);
    	refreshView();
    }
    public void doDaySAT(View v)
    {
    	jCalendar.setWhenToShow(jCalendar.SAT);
    	refreshView();
    }
    public void doDayMTWRF(View v)
    {
    	int w = jCalendar.MON;
    	w |= jCalendar.TUE;
    	w |= jCalendar.WED;
    	w |= jCalendar.THU;
    	w |= jCalendar.FRI;
    	jCalendar.setWhenToShow(w);
    	refreshView();
    }
    public void doDayAll(View v)
    {
    	int w = jCalendar.SUN;
    	w |= jCalendar.MON;
    	w |= jCalendar.TUE;
    	w |= jCalendar.WED;
    	w |= jCalendar.THU;
    	w |= jCalendar.FRI;
    	w |= jCalendar.SAT;
    	jCalendar.setWhenToShow(w);
    	refreshView();
    }
    public void doDayToday(View v)
    {
    	jCalendar.setWhenToShow(jCalendar.TODAY);
    	refreshView();
    }
    public void doDayTomorrow(View v)
    {
    	jCalendar.setWhenToShow(jCalendar.TOMORROW);
    	refreshView();
    }
}
