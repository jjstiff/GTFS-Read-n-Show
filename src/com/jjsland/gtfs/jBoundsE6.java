package com.jjsland.gtfs;

import com.google.android.maps.GeoPoint;

public class jBoundsE6
{
	int lt, ht, ln, hn;
	//public jBoundsE6()
	//{	
	//	lt = 0; ht = 0; ln = 0; hn = 0;
	//}
	public jBoundsE6(jBoundsE6 copy)
	{
	//	this();
		set(copy);
	}
	public jBoundsE6( GeoPoint p )
	{
		lt = ht = p.getLatitudeE6();
		ln = hn = p.getLongitudeE6();
	}
	
	public void add( jBoundsE6 b )
	{
		if( b == null ) return;
		if( b.lt < lt ) lt = b.lt;
		if( b.ln < ln ) ln = b.ln;
		if( ht < b.ht ) ht = b.ht;
		if( hn < b.hn ) hn = b.hn;
	}
	public void set( jBoundsE6 b )
	{
		if( b == null ) return;
		lt = b.lt; ht = b.ht;
		ln = b.ln; hn = b.hn;
	}
	public void add( GeoPoint p )
	{
		if( p == null ) return;
		int t = p.getLatitudeE6();
		int n = p.getLongitudeE6();

		if( t < lt ) lt = t; if( n < ln ) ln = n;
		if( ht < t ) ht = t; if( hn < n ) hn = n;
	}
	public void set( GeoPoint p )
	{
		if( p == null ) return;
		lt = ht = p.getLatitudeE6();
		ln = hn = p.getLongitudeE6();
	}
	public boolean equals(jBoundsE6 cmp)
	{
		if( cmp == null ) return false;
		if( cmp == this ) return true;
		return ( ln == cmp.ln && hn == cmp.hn && lt == cmp.lt && ht == cmp.ht );
	}
	
	public GeoPoint getCenter() { return new GeoPoint((lt+ht)/2,(ln+hn)/2); }
	public int getLatSpanE6() { return (ht - lt); }
	public int getLonSpanE6() { return (hn - ln); }
	public int getLowLatE6() { return lt; }
	public int getLowLonE6() { return ln; }
	public int getHighLatE6() { return ht; }
	public int getHighLonE6() { return hn; }
}
