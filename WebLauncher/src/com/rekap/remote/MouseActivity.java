package com.rekap.remote;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.avidandrew.weblauncher.*;
import com.rekap.network.NetInput;
import com.rekap.network.Network;

public class MouseActivity extends Activity {

	/**
	 * Left mouse click
	 */
    OnClickListener leftEvent = new OnClickListener() {
        public void onClick(View v) {
            NetInput.LeftClick();
        }
    };

    /**
     * Right mouse click
     */
    OnClickListener rightEvent = new OnClickListener() {
        public void onClick(View v) {
            NetInput.RightClick();
        }
    };

    /**
     * Bring up the keyboard on the Mouse Activity
     */
    OnClickListener menuEvent = new OnClickListener() {
        public void onClick(View v) {
        	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        	imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    NetInput.VolumeUp();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    NetInput.VolumeDown();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    private KeypadHandler keypadHandler = new KeypadHandler();
    private RelativeLayout layout;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mouse_activity_actions, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
  	  switch (item.getItemId()) {
  	    case R.id.action_mouse:
  	      startActivity(new Intent(getBaseContext(), MouseActivity.class));
  	      return true;
  	    case R.id.reload:
  	      // since we're on the mouse page, reconnect to the mouse server
  	      MainActivity.findServers();
  	      Network.Connect(Globals.Server);
  	      return true;
  	    case R.id.action_settings:
  	    	startActivity(new Intent(getBaseContext(), Preferences.class));
  	    default:
  	      return super.onOptionsItemSelected(item);
  	  }
  	}    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTheme(R.style.AppTheme);
        setContentView(R.layout.main);

        layout = (RelativeLayout)findViewById(R.id.background);
        final Button leftClick = (Button)findViewById(R.id.leftClick);
        final Button rightClick = (Button)findViewById(R.id.rightClick);
        final ImageButton menuClick = (ImageButton)findViewById(R.id.keyboardButton);

        layout.setOnTouchListener(new TouchpadHandler());
        layout.setOnKeyListener(keypadHandler);
        leftClick.setOnClickListener(leftEvent);
        rightClick.setOnClickListener(rightEvent);
        menuClick.setOnClickListener(menuEvent);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        loadPreferences(this);
    }

    /**
     * Load preferences for use in the application, and scan the network for available servers
     * @param a the Activity to use
     */
    public static void loadPreferences(final Activity a)
    {
    	// attempt to detect servers again
    	MainActivity.findServers();
    	
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(a.getBaseContext());

        Globals.AutoConnect = prefs.getBoolean(Globals.AUTOCONNECT, true);
        Globals.FirstRun = prefs.getBoolean(Globals.FIRSTRUN, true);
        Globals.Sensitivity = ((float)(prefs.getInt(Globals.SENSITIVITY, 50) + 20)) / 100;
        
        // get the preferred server
        String preferred = prefs.getString(Globals.SERVER, null);
        
        if (preferred == null || !MainActivity.isValidServer(preferred)) {
        	// either we don't have a preferred server, or the one that is set is inaccessible, so use the first accessible one
        	Globals.Debugger("Mouse", "Preferred not set or offline, using first accessible");
        	Globals.Server = Network.GetFirstServer();
        } else {
        	// we have a preferred server set and it is accessible, so use it
        	Globals.Server = preferred;
        	Globals.Debugger("Mouse", "Using preferred, accessible server");
        }
        if (Globals.Server == null) {
        	Globals.Debugger("Mouse", "No servers found, rescheduling...");
        	// no preferred or accessible server found, reschedule this action
    		new Timer().schedule(new TimerTask() {          
    			@Override
    			public void run() {
    				loadPreferences(a);	     
    			}
    		}, 1000);	
        } else {
        	Globals.Debugger("Mouse", "Mouse Server: " + Globals.Server);
        }
        
        Globals.Server_URL = prefs.getString(Globals.SERVER_URL, Globals.Server_URL_Default);

        if (Globals.FirstRun) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Globals.FIRSTRUN, false);
            editor.commit();
        }
    }
}
