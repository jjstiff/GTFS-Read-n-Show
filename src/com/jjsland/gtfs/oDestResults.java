package com.jjsland.gtfs;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class oDestResults extends Overlay implements ListAdapter 
{
	List<Address> mData;
	
	public oDestResults()
	{
		mData = null;
	}
	
	public void setData(List<Address> addr)
	{
		mData = addr;
	}

	static LayoutInflater mInflater;
	static LayoutInflater getLayoutInflater(Context c)
	{
		if( mInflater == null )
			mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return mInflater;
	}
	
	final static int GEO(double v) { return (int)(v*1000000); }
	
	static Paint COLOR_DEST = new Paint();
	static Paint COLOR_SHADOW = new Paint();
	static {
		COLOR_DEST.setARGB(255,125,255,125); COLOR_DEST.setStrokeWidth(3);
		COLOR_SHADOW.setARGB(255,200,200,200); COLOR_SHADOW.setStrokeWidth(3);
	}

	public void draw(Canvas c, MapView mv, boolean shadow)
	{
		Projection p = mv.getProjection();
		GeoPoint gp;
		Point ap = new Point();
		Address a;
		
		if( mData == null || shadow ) return;
		
		Iterator<Address> i = mData.iterator();
		while( i.hasNext() )
		{
			a = i.next();
			gp = new GeoPoint(GEO(a.getLatitude()),GEO(a.getLongitude()));
			p.toPixels(gp,ap);
			c.drawCircle(ap.x,ap.y,8,COLOR_DEST);
		}
	}
	
	/**
	 * ListAdapter Methods
	 */
	
	public int getCount() { return mData == null ? 0 : mData.size(); }
	public Object getItem(int i) { return mData == null ? null : mData.get(i); }
	public long getItemId(int p) { return p; }
	public int getItemViewType(int p) { return 0; }
	public int getViewTypeCount() { return 1; }

	@Override
	public View getView(int p, View cView, ViewGroup parent)
	{
		LayoutInflater inflater = getLayoutInflater(parent.getContext());
		
		if(cView == null)
			cView = inflater.inflate(R.layout.ed_addr_row,null);

		Address a = mData.get(p);
		TextView tv = (TextView) cView.findViewById(R.id.edAddr1);
		String s = a.getAddressLine(0);
		tv.setText(s);
		tv = (TextView) cView.findViewById(R.id.edAddr2);
		if(a.getMaxAddressLineIndex() >= 1)
			s = a.getAddressLine(1);
		else
			s = a.getLocality() + " " + a.getPostalCode();
		tv.setText(s);
		
		return cView;
	}
	
	public boolean hasStableIds() { return false; }
	public boolean isEmpty() { return mData == null || mData.size() == 0; }

	public void registerDataSetObserver(DataSetObserver observer) {}
	public void unregisterDataSetObserver(DataSetObserver observer) {}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnabled(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
