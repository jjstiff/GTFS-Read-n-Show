package com.jjsland.gtfs;

import java.util.GregorianCalendar;

import com.google.android.maps.GeoPoint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class aShowRoute extends aSuperMap implements OnItemClickListener, OnScrollListener
{
	static final String ROUTE = "route";
	
	jRoute mRoute;
	oRouteOverlay mRouteOverlay;
	laRoute mRouteList;
	ListView mRouteListView;
	int mSelected = 0;
	int mScrollPos = 0;
	int mScrollSize = 0;
	
	@Override
	public boolean isRouteDisplayed() { return false; }
	
	@Override
	public void onCreate(Bundle s)
	{
		super.onCreate(s);
		
		LayoutInflater l = getLayoutInflater();
		View head = l.inflate(R.layout.showroute_head, null);
		View foot = l.inflate(R.layout.showroute_foot, null);
		View dayButtons = l.inflate(R.layout.sub_day_selector, null);
		ViewGroup g = (ViewGroup) findViewById(R.id.superHeadView);
		g.addView(dayButtons);
		g.addView(head);
		g = (ViewGroup) findViewById(R.id.superFootView);
		g.addView(foot);
		Bundle extras = getIntent().getExtras();
		int route_id = ( extras == null ? 0 : extras.getInt(aShowRoute.ROUTE) );
		if( route_id > 0 )
			mRoute = gtfsApp.getFirstLoader().getRouteById(route_id);
		else
			mRoute = createTestRoute();
		mRouteOverlay = new oRouteOverlay(this,getMapView());
		mRouteOverlay.setRoute(mRoute);
		addOverlay(mRouteOverlay);
		zoomToRoute(mRoute);
		
		mRouteList = new laRoute(mRoute);
		mRouteListView = (ListView) findViewById(R.id.srfListTimes);
		if( mRouteListView != null )
		{
			mRouteListView.setOnItemClickListener(this);
			mRouteListView.setOnScrollListener(this);
		}
		
		GeoPoint loc = mLocation.getMyLocation();
		if( loc != null )
		{
			int i = mRoute.findClosestStopIndex(loc.getLatitudeE6(),loc.getLongitudeE6());
			jStop stop = mRoute.getStopAtIndex(i);
			mRouteOverlay.setHighlight(stop);
			setHighlight(i,stop);
		}
		mRouteList.setTripToNow();
		
		resetListAdapter();
		resetHeader();
		enableRouteButtons();
		refreshDayButtons();
	}
	
	void resetHeader()
	{
		TextView tv = (TextView) findViewById(R.id.srhTitle);
		if( tv != null && mRoute != null ) tv.setText(mRoute.getSelectedSeqName());
	}
	
	void resetListAdapter()
	{
		if( mRouteListView != null )
		{
			mRouteListView.setAdapter(mRouteList);
			if( mSelected < mScrollPos || mSelected >= mScrollPos + mScrollSize )
			{
				mScrollPos = mSelected - mScrollSize/2;
				if( mScrollPos < 0 ) mScrollPos = 0;
			}
			mRouteListView.setSelection(mScrollPos);
		}
	}
	
	static final int TEST_ID = -55;
	jRoute createTestRoute()
	{
		jRoute r = new jRoute(TEST_ID,"tagn","Test Route","testing");
		GregorianCalendar today = new GregorianCalendar();
		jCalendar c = new jCalendar(TEST_ID,today,today,1,1,1,1,1,1,1);
		jTrip t1 = new jTrip(TEST_ID+1,c,r); r.addTripInSeqById("1st Trip of Test",t1);
		jTrip t2 = new jTrip(TEST_ID+2,c,r); r.addTripInSeqById("2nd Trip of Test",t2);
		jTrip t3 = new jTrip(TEST_ID+3,c,r); r.addTripInSeqById("3rd Trip of Test",t3);
		jStop s1 = new jStop(TEST_ID+1,"Test Stop 1",(int)(37.42f*10e6),(int)(-122.08f*10e6));
		jStop s2 = new jStop(TEST_ID+2,"Test Stop 2",(int)(37.40f*10e6),(int)(-122.08f*10e6));
		jStop s3 = new jStop(TEST_ID+3,"Test Stop 3",(int)(37.38f*10e6),(int)(-122.08f*10e6));
		jStop s4 = new jStop(TEST_ID+4,"Test Stop 4",(int)(37.36f*10e6),(int)(-122.08f*10e6));
		jStop s5 = new jStop(TEST_ID+5,"Test Stop 5",(int)(37.34f*10e6),(int)(-122.08f*10e6));
		jStop s6 = new jStop(TEST_ID+6,"Test Stop 6",(int)(37.32f*10e6),(int)(-122.08f*10e6));
		jStop s7 = new jStop(TEST_ID+7,"Test Stop 7",(int)(37.30f*10e6),(int)(-122.08f*10e6));
		jStop s8 = new jStop(TEST_ID+8,"Test Stop 8",(int)(37.28f*10e6),(int)(-122.08f*10e6));
		jStop s9 = new jStop(TEST_ID+7,"Test Stop 9",(int)(37.26f*10e6),(int)(-122.08f*10e6));
		jStop s10 = new jStop(TEST_ID+8,"Test Stop 10",(int)(37.24f*10e6),(int)(-122.08f*10e6));
		t1.addStopTime(s1,1,tdStopTimes.parseTime("03:31:00"));
		t1.addStopTime(s2,2,tdStopTimes.parseTime("03:35:00"));
		t1.addStopTime(s3,3,tdStopTimes.parseTime("03:39:00"));
		t1.addStopTime(s4,4,tdStopTimes.parseTime("03:44:00"));
		t1.addStopTime(s5,5,tdStopTimes.parseTime("03:48:00"));
		t1.addStopTime(s6,6,tdStopTimes.parseTime("03:53:00"));
		t1.addStopTime(s7,7,tdStopTimes.parseTime("03:56:00"));
		t1.addStopTime(s8,8,tdStopTimes.parseTime("03:59:00"));
		t1.addStopTime(s9,9,tdStopTimes.parseTime("04:01:00"));
		t1.addStopTime(s10,10,tdStopTimes.parseTime("04:03:00"));
		t2.addStopTime(s4,1,tdStopTimes.parseTime("04:31:00"));
		t2.addStopTime(s3,2,tdStopTimes.parseTime("04:35:00"));
		t2.addStopTime(s2,3,tdStopTimes.parseTime("04:39:00"));
		t2.addStopTime(s1,4,tdStopTimes.parseTime("04:44:00"));
		return r;
	}
	
	@Override
	void refreshDayButtons()
	{
		super.refreshDayButtons();
		if( mRouteList != null )
		{
			mRouteList.resetTrip_whenToShowChanged();
			resetListAdapter();
		}
	}
	
	public void enableRouteButtons()
	{
		Button b = (Button) findViewById(R.id.bsrfPrev);
		if( b != null ) b.setEnabled(mRouteList != null && mRouteList.hasPrevTrip());
		b = (Button) findViewById(R.id.bsrfNow);
		if( b != null ) b.setEnabled(mRouteList != null);
		b = (Button) findViewById(R.id.bsrfNext);
		if( b != null ) b.setEnabled(mRouteList != null && mRouteList.hasNextTrip());
		b = (Button) findViewById(R.id.bsrfDir1);
		if( b != null ) b.setEnabled(mRoute != null && mRouteList != null);
		b = (Button) findViewById(R.id.bsrfDir2);
		if( b != null ) b.setEnabled(mRoute != null && mRouteList != null);
	}
	
	public void doSeqPrev(View v)
	{
		mRoute.decSelectedSeq();
		resetListAdapter();
		resetHeader();
		zoomToRoute(mRoute);
		enableRouteButtons();
	}
	
	public void doSeqNext(View v)
	{
		mRoute.incSelectedSeq();
		resetListAdapter();
		resetHeader();
		zoomToRoute(mRoute);
		enableRouteButtons();
	}
	
	public void doTimePrev(View v)
	{
		mRouteList.goPrevTrip();
		resetListAdapter();
		refreshMap();
		enableRouteButtons();
	}
	public void doTimeNext(View v)
	{
		mRouteList.goNextTrip();
		resetListAdapter();
		refreshMap();
		enableRouteButtons();
	}
	public void doTimeNow(View v)
	{
		mRouteList.setTripToNow();
		resetListAdapter();
		refreshMap();
		enableRouteButtons();
	}
	public void doLoc(View v)
	{
		GeoPoint loc = mLocation.getMyLocation();
		if( loc != null )
		{
			int i = mRoute.findClosestStopIndex(loc.getLatitudeE6(),loc.getLongitudeE6());
			jStop s = mRoute.getStopAtIndex(i);
			mRouteOverlay.setHighlight(s);
			setHighlight(i,s);
		}
		resetListAdapter();
		refreshMap();
		enableRouteButtons();
	}
	
	void setHighlight(int p, jStop s)
	{
		mSelected = p;
		if( mRouteList != null ) {
			mRouteList.setHighlight(s);
			resetListAdapter();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg, View inView, int p, long arg3)
	{
		mSelected = p;
		jStop s = (jStop) mRouteList.getItem(p);
		mRouteList.setHighlight(s);
		if( s != null && mRouteOverlay != null )
		{
			mRouteOverlay.setHighlight(s);
		}
		resetListAdapter();
	}

	@Override
	public void onScroll(AbsListView view, int firstI, int vCount, int tCount) {
		mScrollPos = firstI;
		mScrollSize = vCount;
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// TODO Auto-generated method stub
	}

}
