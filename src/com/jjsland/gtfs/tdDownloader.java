package com.jjsland.gtfs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.util.Log;

public class tdDownloader {

	private String mURL;
	private String mSaveName;
	private boolean mSaved;
	
	static final String ZFILE_AGENCY = "agency.txt";
	static final String ZFILE_CAL = "calendar.txt";
	static final String ZFILE_STOPS = "stops.txt";
	static final String ZFILE_ROUTES = "routes.txt";
	static final String ZFILE_TRIPS = "trips.txt";
	static final String ZFILE_FARE_ATTR = "fare_attributes.txt";
	static final String ZFILE_FARE_RULE = "fare_rules.txt";
	static final String ZFILE_STOP_TIMES = "stop_times.txt";
	
	void deleteDataFile()
	{
		Context c = gtfsApp.getStaticContext();
		if(c!=null) c.deleteFile(mSaveName);
		mSaved = false;
	}
	
	void checkDataFile()
	{
		mSaved = false;
		
		Context c = gtfsApp.getStaticContext(); if( c == null ) return;
		
		String[] f = c.fileList();
		for( int i = 0; i < f.length; i++ )
		{
			if( f[i].matches(mSaveName) )
			{
				// TODO: Download new file when calendar is out of date.
				// But save current until new one is downloaded properly.
				mSaved = true;
				return;
			}
		}
	}
	
	public boolean hasDownloadedData()
	{
		return mSaved;
	}
	
	public tdDownloader(String url, String savename)
	{
		mURL = url;
		mSaveName = savename;
		checkDataFile();
	}
	
	public void downloadData() throws Exception
	{
		checkDataFile(); if( mSaved ) return;
		
		Context c = gtfsApp.getStaticContext();
		if( c == null ) throw new Exception("tdDownloader: Static Context not Available");
		
		try {
			FileOutputStream fos = c.openFileOutput(mSaveName,Context.MODE_PRIVATE);
			BufferedOutputStream bout = new BufferedOutputStream(fos);
			
			URL url = new URL(mURL);
			URLConnection conn = url.openConnection();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			int data;
			while((data = bis.read()) != -1)
			{
				bout.write(data);
			}
			bout.close();
			mSaved = true;
		}
		catch(IOException e)
		{
			throw new Exception("tdDownloader: Error Downloading Data.",e);
		}
		
	}
	
	protected jZipData getZippedFile(String name)
	{
		Context c = gtfsApp.getStaticContext(); if( c == null ) return null;
		
		try {
			FileInputStream f = c.openFileInput(mSaveName);
			ZipInputStream z = new ZipInputStream(f);
			ZipEntry e;
			while( (e = z.getNextEntry()) != null )
			{
				if(e.getName().matches(name))
					return new jZipData(e,z);
			}
			Log.e("tdDownloader.getZippedFile","Could not find \"" + name + "\n");
		}
		catch( IOException e ) 
		{
			Log.e("tdDownloader.getZippedFile","Error opening \"" + name + "\n");
		}
		return null;
	}
	
	public jZipData getCalendar() { return getZippedFile(ZFILE_CAL); }
	public jZipData getStops() { return getZippedFile(ZFILE_STOPS); }
	public jZipData getRoutes() { return getZippedFile(ZFILE_ROUTES); }
	public jZipData getTrips() { return getZippedFile(ZFILE_TRIPS); }
	public jZipData getStopTimes() { return getZippedFile(ZFILE_STOP_TIMES); }
	public jZipData getAgency() { return getZippedFile(ZFILE_AGENCY); }
	
	public String getZippedContentString()
	{
		if( !mSaved ) {
			return "File has not been Downloaded.";
		}
		String out = "";
		try {
			Context c = gtfsApp.getStaticContext();
			FileInputStream f = c.openFileInput(mSaveName);
			FileChannel fc = f.getChannel();
			out += mSaveName + " is " + fc.size() + " bytes.\n";
			ZipInputStream z = new ZipInputStream(f);
			ZipEntry e;
			while( (e = z.getNextEntry()) != null )
			{
				out += zipEntryString(e);
			}
			return out;
		}
		catch( IOException e)
		{
			return out + "Error reading zipped contents.\n";
		}
	}
	
	static String zipEntryString(ZipEntry e)
	{
		String out = "--- " + e.getName() + " is " + e.getSize() + " bytes.";
		out += "(zip " + (float)(e.getCompressedSize()*1000/e.getSize())/10 + "%)\n";
		return out;
	}
}
