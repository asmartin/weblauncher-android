package com.avidandrew.weblauncher;

import android.app.ActionBar;
import android.app.Activity;
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
public class WebLauncherActivity extends Activity {
	private WebView myWebView = null;
	private final String APP = "Web Launcher";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_weblauncher);
        
        // show back button
        ActionBar actionBar = getActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        
        myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(webSettings.getUserAgentString() + APP);
        reloadPage();
        myWebView.setWebViewClient(new WebViewClient());
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	  switch (item.getItemId()) {
    	    case android.R.id.home:
              // app icon in action bar clicked; go home
              Intent intent = new Intent(this, MainActivity.class);
              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              startActivity(intent);
              return true;
    	    case R.id.reload:
    	      reloadPage();
    	      return true;
    	    default:
    	      return super.onOptionsItemSelected(item);
    	  }
    	}
    	    
	private void reloadPage() {
		myWebView.loadUrl("http://axe/stream");
	}
}