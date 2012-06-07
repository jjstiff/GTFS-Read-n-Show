package com.jjsland.gtfs;

public interface GTFSLoadListener {
	/**
	 * 
	 * @param step - the step that has been completed: see GTFSLoader constants.
	 * @param count - number of items loaded by this step. -1 if the listener is added after the step is complete
	 * @param time - nanos to complete this step. -1 if the listener is added after the step is complete.
	 */
	public void onGTFSLoadEvent(int step, long time);
	public void onGTFSLoadStopTimeUpdate(int count, long bytesRead, long bytesTotal);
}
