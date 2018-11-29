package com.avidandrew.weblauncher;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.rekap.network.Network;
import com.rekap.remote.*;
import tk.nfsmonstr.simplewakeonlan.*;

public class MainActivity extends Activity {
	private WebView myWebView = null;
	private final String APP = "Web Launcher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weblauncher);

        MouseActivity.loadPreferences(this);

		getActionBar().show();

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
    	  	case R.id.action_wol:
				  startActivity(new Intent(getBaseContext(), WOLActivity.class));
				  return true;
    	    case R.id.action_mouse:
    	      startActivity(new Intent(getBaseContext(), MouseActivity.class));
    	      return true;
    	    case R.id.reload:
    	      // since we're on the WebView page, reload the page
    	      reloadPage();
    	      return true;
    	    case R.id.action_settings:
    	    	startActivity(new Intent(getBaseContext(), Preferences.class));
			  case R.id.menu_about:
				  startActivity(new Intent(getBaseContext(), AboutActivity.class));
				  return true;
    	    default:
    	      return super.onOptionsItemSelected(item);
    	  }
    	}


    /**
     * Kicks off the locator process to look for available servers
     */
    public static void findServers() {
		Log.d("WebLauncher", "findServers");
    	Network.LocatorStart();
    }

    /**
     * checks if the supplied server is still in the list of valid (and accessible) servers
     * @param name the hostname of the server to test
     * @return true if it is still valid and accessible, false otherwise
     */
    public static boolean isValidServer(String name) {
		Log.d("WebLauncher", "isValidServer");
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
		String url = null;

		Log.d("WebLauncher", "settings Server_URL: " + Globals.Server_URL);
		//Log.d("WebLauncher", "settings SERVER_URL: " + Globals.SERVER_URL);
		Log.d("WebLauncher", "settings Server: " + Globals.Server);
		Log.d("WebLauncher", "settings AutoConnect: " + Globals.AutoConnect);
		Log.d("WebLauncher", "settings WOL_Server: " + Globals.WOL_Server);
		if (Globals.Server_URL != null && !Globals.Server_URL.equals("")) {
			Log.d("WebLauncher", "custom: " + Globals.Server_URL);
			// custom server is defined, use it
			String custom = Globals.Server_URL;
			url = "http://" + custom.replaceAll("http://", "");
		} else if (Globals.WOL_Server != null) {
			Log.d("WebLauncher", "wol: " + Globals.WOL_Server);
			// a WOL server was selected, use it
			url = "http://" + Globals.WOL_Server;
		} else if (Globals.AutoConnect) {
			Log.d("WebLauncher", "auto-connect, searching...");
			if (isValidServer(Globals.Server)) {
				Log.d("WebLauncher", "auto-connect selected: " + Globals.Server);
				url = "http://" + Globals.Server;
			} else {
				String firstServer = Network.GetFirstServer();
				if (firstServer == null) {
					// no auto-discovered servers, error
					Log.d("WebLauncher", "auto-connect: no auto-discovered servers");
					final Context parent = this;
					this.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(parent,"No auto-discovered viewers found; will retry shortly... ",Toast.LENGTH_LONG).show();
							new Timer().schedule(new TimerTask() {
								@Override
								public void run() {
									reloadPage();
								}
							}, 8000);
						}
					});
				} else {
					Log.d("WebLauncher", "auto-connect first server:" + firstServer);
					url = "http://" + firstServer;
				}
			}
		} else {
			// no servers specified, suggest using auto-discover
			Log.d("WebLauncher", "no servers specified");
			final Context parent = this;
			this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(parent,"No viewer specified; you may want to enable Auto-Discover in Settings ",Toast.LENGTH_LONG).show();
				}
			});
		}

		if (url != null) {
			// a valid URL was found, try to load it
			Globals.Debugger("Weblauncher", "URL: " + url);
			final String finalUrl = url;
			final Context parent = this;
			this.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(parent, "Loading " + finalUrl, Toast.LENGTH_SHORT).show();
				}
			});

			// load the URL, or if it isn't available an error page
			final URLChecker loader = new URLChecker();
			loader.execute(url);
		}
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
    					+ "and select \"" + getResources().getString(R.string.reload_title) + "\".</p><p>A viewer will be loaded using the following methods (in this order):<ul><li>Custom Viewer - if you have set a Custom Viewer in the Settings page, it will be used</li>" +
						"<li>WOL - if you selected a viewer on the WOL (power) page, that viewer will be used</li><li>Auto-Discover - if you have enabled Auto-Discover in the Settings page, the network will be scanned and the first viewer found will be used</li></ul>" +
						"</p></body></html>";
    			myWebView.loadDataWithBaseURL("", data, "text/html", "UTF-8", "");
    		}
    	}
    }
}