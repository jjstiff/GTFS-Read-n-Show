package com.jjsland.gtfs;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;

public class gtfsApp extends Application {
	
	private static gtfsApp smRoot;
	private ArrayList<GTFSLoader> mData;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		if( smRoot == null ) { smRoot = this; }
		else
		{
			// TODO: Explain that this is not the first gtfsApp
			// Launch that app instread... ?
		}
		mData = new ArrayList<GTFSLoader>();
		// TODO: Load Data Sets from persistant storage.
		// TODO: Load Data Set List from jjsland.
		
		String name = "VTA - Santa Clara, CA";
		String gtfs_url = "http://www.vta.org/dev/data/google_transit.zip";
		String gtfs_save = "vta_data";
		GTFSLoader primera = new GTFSLoader(name,gtfs_url,gtfs_save);
		mData.add(primera);
		
	}

	public static Context getStaticContext()
	{
		return ( smRoot == null ? null : smRoot.getApplicationContext() );
	}
	
	public static GTFSLoader getFirstLoader()
	{
		if( smRoot == null ) return null;
		return smRoot.getLoader(0);
	}
	
	GTFSLoader getLoader(int index)
	{
		if( mData == null || index < 0 || index >= mData.size() )
			return null;
		return mData.get(0);
	}
	

}
