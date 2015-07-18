package com.avidandrew.weblauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
     * Reload the webpage
     */
	private void reloadPage() {
		String url = "";
		
		if (!Globals.Server_URL.equals("")) {
		//	url = Globals.Server_URL;
			String custom = Globals.Server_URL;
			url = "http://" + custom.replaceAll("http://", "");
		} else {
			// load the same hostname as the auto-detected viewer
			url = "http://" + Globals.Server;
		}
		
		// load the URL
		myWebView.loadUrl(url);
	}
	
    @Override
    public void onResume()
    {
        super.onResume();

        MouseActivity.loadPreferences(this);
        reloadPage();
    }

}