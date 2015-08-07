package com.avidandrew.weblauncher;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.rekap.network.Network;
import com.rekap.remote.*;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity {
	private WebView myWebView = null;
	private final String APP = "Web Launcher";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_weblauncher);
        
        MouseActivity.loadPreferences(this);
        
        myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(webSettings.getUserAgentString() + APP);
        reloadPage();
        myWebView.setWebViewClient(new WebViewClient());
        
        if (Globals.FirstRun) {
            new AlertDialog.Builder(this)
                .setMessage(R.string.firstruntext)
                .setNeutralButton("OK", null)
                .show();
            Globals.FirstRun = false;
        }
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	  switch (item.getItemId()) {
    	    case R.id.action_mouse:
    	      startActivity(new Intent(getBaseContext(), MouseActivity.class));
    	      return true;
    	    case R.id.reload:
    	      // since we're on the WebView page, reload the page
    	      reloadPage();
    	      return true;
    	    case R.id.action_settings:
    	    	startActivity(new Intent(getBaseContext(), Preferences.class));
    	    default:
    	      return super.onOptionsItemSelected(item);
    	  }
    	}
    
    
    /**
     * Kicks off the locator process to look for available servers
     */
    public static void findServers() {
    	Network.LocatorStart();
    }
    
    /**
     * checks if the supplied server is still in the list of valid (and accessible) servers
     * @param name the hostname of the server to test
     * @return true if it is still valid and accessible, false otherwise
     */
    public static boolean isValidServer(String name) {
        	findServers();
        	String[] servers = Network.GetServers();
        	for (String s : servers) {
        		if (s.equals(name)) {
        			return true;
        		}
        	}
        	return false;
    }
    
    /**
     * Reload the webpage
     */
	private void reloadPage() {
		String url = "";
		
		// rescan for available servers
		findServers();		
		
		if (Globals.Server == null) {
			// no accessible servers found, schedule this task to run again later
			Globals.Debugger("Webpage", "No accessible servers found, rescheduling page load ...");
			new Timer().schedule(new TimerTask() {          
			    @Override
			    public void run() {
			    	 reloadPage();    
			    }
			}, 2000);
		} else {
			if (Globals.Server_URL != null && !Globals.Server_URL.equals("")) {
				//	url = Globals.Server_URL;
					String custom = Globals.Server_URL;
					url = "http://" + custom.replaceAll("http://", "");
				} else {
					// load the same hostname as the auto-detected viewer
					if (isValidServer(Globals.Server)) {
						url = "http://" + Globals.Server;
					} else {
						url = "http://" + Network.GetFirstServer();
					}
				}
		}
		
		Globals.Debugger("Webpage", "URL: " + url);
		
		// load the URL, or if it isn't available an error page
		final URLChecker loader = new URLChecker();
		loader.execute(url);
			    
	}
	
    @Override
    public void onResume()
    {
        super.onResume();

        MouseActivity.loadPreferences(this);
        reloadPage();
    }

    /**
     * Checks to see if the provided URL is accessible or not
     * @author amartin
     *
     */
    private class URLChecker extends AsyncTask<String, String, String> {

    	private boolean response = false;
    	private String tryURL = "";

    	/**
    	 * returns true if the provided URL is accessible (HTTP Code 200)
    	 * @param link the URL to test
    	 * @return true if it is accessible, false otherwise
    	 */
    	private boolean isWebpageAccessible(String link) {
    		int httpCode = 500;
			try {
		    	URL url = new URL(link);
		    	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		    	if (connection.getResponseCode() > 0) {
		    		httpCode = connection.getResponseCode();
		    	}
			} catch (IOException ioe) {
				return false;
			}
			return httpCode == 200;
    	}

    	@Override
    	protected String doInBackground(String... params) {
    		tryURL = params[0];
    		
    		//publishProgress("Sleeping..."); // Calls onProgressUpdate()
    		try {
    			// check if the webpage is accessible
    			response = isWebpageAccessible(tryURL);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		
    		if (response) {
    			return tryURL;
    		}
    		
    		return null;
    	}

    	/*
    	 * (non-Javadoc)
    	 * 
    	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
    	 */
    	@Override
    	protected void onPostExecute(String result) {
    		
    		if (result != null) {
    			// load the URL
    			myWebView.loadUrl(result);
    		} else {
    			String data = "<html><body><h1 style='text-align:center'>Viewer Offline</h1><p>The viewer (" + tryURL + ") is not accessible. "
    					+ "Please verify that it is online. To attempt to reconnect, press the &lt;Menu&gt; button "
    					+ "and select \"" + getResources().getString(R.string.reload_title) + "\".</p></body></html>";
    			myWebView.loadDataWithBaseURL("", data, "text/html", "UTF-8", "");
    		}
    	}
    }
}