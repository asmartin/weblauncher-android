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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(a.getBaseContext());

        Globals.AutoConnect = prefs.getBoolean(Globals.AUTOCONNECT, true);
        Globals.FirstRun = prefs.getBoolean(Globals.FIRSTRUN, true);
        Globals.Sensitivity = ((float)(prefs.getInt(Globals.SENSITIVITY, 50) + 20)) / 100;
        Globals.Server_URL = prefs.getString(Globals.SERVER_URL, "");

        if (Globals.FirstRun) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Globals.FIRSTRUN, false);
            editor.commit();
        }
    }
}
