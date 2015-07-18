package com.rekap.remote;

public class Globals {
	public static String Server_URL_Default = "http://avidandrew.com/weblauncher-setup.html";
	
	public static String connectedHost = "First";
	
	//increment firstrun with each app release
	public static final String AUTOCONNECT = "autoconnect", SERVER = "server", SERVER_URL = "server_url",
			SENSITIVITY = "sensitivity", FIRSTRUN = "firstrun1"; 
	
	public static boolean AutoConnect, FirstRun;
	public static String Server;		// weblauncher-viewer (where mouse server is running)
	public static String Server_URL;	// weblauncher-server (where links webpage is hosted)
	public static float Sensitivity;
}
