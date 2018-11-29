package com.rekap.remote;

import android.util.Log;

public class Globals {
	public static boolean DEBUG = false;
	
	//increment firstrun with each app release
	public static final String AUTOCONNECT = "autoconnect", SERVER = "server", SERVER_URL = "server_url",
			SENSITIVITY = "sensitivity", FIRSTRUN = "firstrun1"; 
	
	public static boolean AutoConnect, FirstRun;
	public static String Server;		// selected server from auto-discovered list
	public static String Server_URL;	// custom server hostname
	public static String WOL_Server = null; // selected WOL server hostname
	public static float Sensitivity;	// how sensitive the mouse should be
	
	
	/**
	 * Logging function that logs messages if the DEBUG flag is set
	 * @param tag tag to associate with the message
	 * @param msg the message itself
	 */
	public static void Debugger(String tag, String msg) {
		if (DEBUG) {
			Log.i(tag, msg);
		}
	}
}
