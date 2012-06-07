package com.jjsland.gtfs;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class laRoute implements ListAdapter
{
	jRoute mRoute;
	jStop mHighlight;
	
	static final int COLOR_HIGHLIGHT = 0x00FF00FF;
	static final int COLOR_BASIC = 0x000000;
	
	public laRoute()
	{
		mRoute = null;
		mHighlight = null;
	}
	public laRoute(jRoute r) { this(); setRoute(r); }
	public void setRoute(jRoute r) { mRoute = r; }
	
	public void setHighlight( jStop s ) { mHighlight = s; }
	
	public void setTripToNow()
	{
		if( mRoute != null )
		{
			jTripSeq jts = mRoute.getSelectedSeq();
			if( jts != null )
				jts.selectTripForNow(mHighlight);
		}
	}
	
	public void resetTrip_whenToShowChanged()
	{
		goNextTrip();
		goPrevTrip();
	}
	
	public boolean hasNextTrip()
	{
		if( mRoute == null ) return false;
		jTripSeq jts = mRoute.getSelectedSeq(); if( jts == null ) return false;
		return jts.hasNextTrip();
	}
	
	public void goNextTrip()
	{
		if( mRoute != null )
		{
			jTripSeq jts = mRoute.getSelectedSeq();
			if( jts != null )
				jts.selectNextTrip();
		}
	}
	
	public boolean hasPrevTrip()
	{
		if( mRoute == null ) return false;
		jTripSeq jts = mRoute.getSelectedSeq(); if( jts == null ) return false;
		return jts.hasPrevTrip();
	}
	
	public void goPrevTrip()
	{
		if( mRoute != null )
		{
			jTripSeq jts = mRoute.getSelectedSeq();
			if( jts != null )
				jts.selectPrevTrip();
		}
	}
	
	/**
	 * ListAdapter Interface Items
	 * 
	 */
	public boolean areAllItemsEnabled() { return true; }
	public boolean isEnabled(int p)
	{
		if( mRoute == null ) return false;
		jTripSeq jts = mRoute.getSelectedSeq(); if( jts == null ) return false;
		jTrip jt = jts.getSelectedTrip(); if( jt == null ) return false;
		return ( p >= 0 && p < jt.getStopCount());
	}
	
	public int getCount()
	{
		if( mRoute == null ) return 0;
		jTripSeq jts = mRoute.getSelectedSeq(); if( jts == null ) return 0;
		jTrip jt = jts.getSelectedTrip(); if( jt == null ) return 0;
		return jt.getStopCount();
	}
	
	public Object getItem(int p)
	{ 
		if( mRoute == null ) return null;
		jTripSeq jts = mRoute.getSelectedSeq(); if( jts == null ) return null;
		jTrip jt = jts.getSelectedTrip(); if( jt == null ) return null;
		return jt.getStop(p);
	}
	public long getItemId(int p) { return p; }
	public int getItemViewType(int p) { return 0; }
	public View getView(int p, View cView, ViewGroup parent)
	{
		if(cView == null)
		{
			LayoutInflater inflater = laStop.getLayoutInflater(parent.getContext());
			cView = inflater.inflate(R.layout.showroute_entry,null);
		}
		if(mRoute == null) return cView;
		jTripSeq jts = mRoute.getSelectedSeq(); if( jts == null ) return cView;
		jTrip jt = jts.getSelectedTrip(); if( jt == null ) return cView;
		jTrip jt2 = jts.getNextTrip();
		jTrip jt3 = jts.getNextTrip2();
		
		TextView stopText = (TextView) cView.findViewById(R.id.sreTextStop);
		jStop s = jt.getStop(p);
		if( s == null )
		{
			stopText.setText("Error: null");
		}
		else if( s == mHighlight )
		{
			stopText.setBackgroundColor(COLOR_HIGHLIGHT);
			stopText.setText("sel: " + s.getTitle());
		}
		else
		{
			stopText.setBackgroundColor(COLOR_BASIC);
			stopText.setText(s.getTitle());
		}
		
		TextView tv1 = (TextView) cView.findViewById(R.id.sreTextTime1);
		TextView tv2 = (TextView) cView.findViewById(R.id.sreTextTime2);
		TextView tv3 = (TextView) cView.findViewById(R.id.sreTextTime3);
		
		if( tv1 != null && jt != null )
			tv1.setText(jt.getTimeString(p));
		if( tv2 != null && jt2 != null )
			tv2.setText(jt2.getTimeString(p));
		if( tv3 != null && jt3 != null )
			tv3.setText(jt3.getTimeString(p));
		
		return cView;
	}
	public int getViewTypeCount() { return 1; }
	public boolean hasStableIds() { return true; }
	public boolean isEmpty()
	{ 
		if( mRoute == null ) return true;
		jTripSeq jts = mRoute.getSelectedSeq(); if( jts == null ) return true;
		jTrip jt = jts.getSelectedTrip(); if( jt == null ) return true;
		return jt.getStopCount() <= 0;
	}
	public void registerDataSetObserver(DataSetObserver o) {}
	public void unregisterDataSetObserver(DataSetObserver o) {}
}
