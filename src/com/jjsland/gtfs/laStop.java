package com.jjsland.gtfs;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

public class laStop implements ListAdapter
{
	jStop mStop;
	
	public laStop(jStop s) { mStop = s; }
	public void setStop(jStop s) { mStop = s; } // TODO: Set Dirty
	
	static LayoutInflater mInflater;
	static LayoutInflater getLayoutInflater(Context c)
	{
		if( mInflater == null )
			mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return mInflater;
	}
	/**
	 * ListAdapter Interface Items
	 * 
	 */
	public boolean areAllItemsEnabled() { return true; }
	public boolean isEnabled(int p) { return 0 <= p && p < mStop.mRoutes.size(); }
	public int getCount() { return (mStop.mRoutes == null) ? 0 : mStop.mRoutes.size(); }
	public Object getItem(int p)
	{
		if( 0 <= p && p < mStop.mRoutes.size() )
			return mStop.mRoutes.get(p);
		return null;
	}
	public long getItemId(int p) { return mStop.mRoutes.get(p).mId; }
	public int getItemViewType(int p) { return 0; }
	public View getView(int p, View cView, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater(parent.getContext());
		if(cView == null)
			cView = inflater.inflate(R.layout.sm_route_row,null);

		jRoute r = mStop.mRoutes.get(p);
		
//		r.selectStop(mStop);
//		String times = r.getTimes(mWhenToShow);
		
		TextView rNum = (TextView) cView.findViewById(R.id.smrrRouteNum);
		TextView rAgen = (TextView) cView.findViewById(R.id.smrrAgency);
		TextView rName = (TextView) cView.findViewById(R.id.smrrLongName);
		TextView rTimes = (TextView) cView.findViewById(R.id.smrrTimes);
		rNum.setText(Integer.toString(r.mId));
		rAgen.setText(jCalendar.whenToString(jCalendar.getWhenToShow()));
		rName.setText(r.mLongName);
		rTimes.setText(r.getNext3TimesString(mStop)); //"laStop: Need 3 Next Times");
		
		return cView;
	}
	public int getViewTypeCount() { return 1; }
	public boolean hasStableIds() { return true; }
	public boolean isEmpty() { return mStop.mRoutes == null || mStop.mRoutes.size() == 0; }
	public void registerDataSetObserver(DataSetObserver o) {}
	public void unregisterDataSetObserver(DataSetObserver o) {}
}
