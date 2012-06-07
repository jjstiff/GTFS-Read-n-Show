package com.jjsland.gtfs;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Region;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class oBoundsOverlay extends Overlay
{
	GeoPoint mLow, mHigh;
	Point pLow, pHigh;
	Paint mPaint;
	float mOvergrowth;
	GeoPoint oLow, oHigh;
	jBoundsE6 mBounds;
	
	public oBoundsOverlay(jBoundsE6 bounds, float overgrowth)
	{
		super();
		mBounds = bounds;
		mLow = new GeoPoint(mBounds.getLowLatE6(),mBounds.getLowLonE6());
		mHigh = new GeoPoint(mBounds.getHighLatE6(),mBounds.getHighLatE6());
		pLow = new Point(0,0);
		pHigh = new Point(0,0);
		mPaint = new Paint();
		mPaint.setARGB(255,255,128,128);
		mPaint.setStyle(Paint.Style.STROKE);
		setOvergrowth(overgrowth);
	}
	
	public oBoundsOverlay(jBoundsE6 bounds)
	{
		this(bounds,1.0f);
	}
	
	public void draw(Canvas c, MapView mv, boolean shadow)
	{
		Projection p = mv.getProjection();
		p.toPixels(oLow,pLow);
		p.toPixels(oHigh,pHigh);
		if( shadow )
		{
			c.save();
			c.clipRect(0,0,c.getWidth(),c.getHeight());
			c.clipRect(pLow.x,pHigh.y,pHigh.x,pLow.y,Region.Op.DIFFERENCE);
			c.drawColor(Color.argb(128,255,255,255));
			c.restore();
		}
		else
			c.drawRect(pLow.x,pHigh.y,pHigh.x,pLow.y,mPaint);
	}
	
	public void setOvergrowth(float scale)
	{
		mOvergrowth = scale;
		if( scale == 1.0 )
		{
			oLow = mLow;
			oHigh = mHigh;
		}
		else
		{
			int ct = (mLow.getLatitudeE6() + mHigh.getLatitudeE6())/2;
			int cn = (mLow.getLongitudeE6() + mHigh.getLongitudeE6())/2;
			int st = (mHigh.getLatitudeE6() - mLow.getLatitudeE6());
			int sn = (mHigh.getLongitudeE6() - mLow.getLongitudeE6());
			oLow = new GeoPoint((int)(ct-((st*scale)/2)),(int)(cn-((sn*scale)/2)));
			oHigh = new GeoPoint((int)(ct+((st*scale)/2)),(int)(cn+((sn*scale)/2)));
		}
	}
	
	public int getLatSpanE6(boolean overgrown)
	{
		int st = (mHigh.getLatitudeE6() - mLow.getLatitudeE6());
		if( overgrown ) return (int) (mOvergrowth*st);
		else return st;
	}
	
	public int getLonSpanE6(boolean overgrown)
	{
		int sn = (mHigh.getLongitudeE6() - mLow.getLongitudeE6());
		if( overgrown ) return (int) (mOvergrowth*sn);
		else return sn;
	}
	
	GeoPoint mCenter = null;
	public GeoPoint getCenterPoint()
	{
		if( mCenter != null ) return mCenter;
		
		mCenter = mBounds.getCenter();
		return mCenter;
	}
}
