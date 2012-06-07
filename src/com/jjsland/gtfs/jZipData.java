package com.jjsland.gtfs;

import java.io.InputStream;
import java.util.zip.ZipEntry;

public class jZipData {
	ZipEntry mEntry;
	InputStream mStream;
	
	public jZipData(ZipEntry e, InputStream i)
	{
		mEntry = e;
		mStream = i;
	}
}
