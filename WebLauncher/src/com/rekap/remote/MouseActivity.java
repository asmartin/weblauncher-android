package com.rekap.remote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
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

    OnClickListener leftEvent = new OnClickListener() {
        public void onClick(View v) {
            NetInput.LeftClick();
        }
    };

    OnClickListener rightEvent = new OnClickListener() {
        public void onClick(View v) {
            NetInput.RightClick();
        }
    };

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

    private Context context;
    private KeypadHandler keypadHandler = new KeypadHandler();
    private RelativeLayout layout;

    public static void showSettings(final Activity a) {
        AlertDialog.Builder builder = new AlertDialog.Builder(a);
        builder.setTitle(R.string.app_name)
               .setItems(R.array.options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                case 0:
                    a.startActivity(new Intent(a.getBaseContext(), Preferences.class));
                    break;

                case 1:
                    InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    imm.showSoftInput((RelativeLayout)a.findViewById(R.id.background), InputMethodManager.SHOW_FORCED);
                    break;

                case 2:
                    Network.Connect(Globals.Server);
                    break;
                }
            }
        });
        builder.show();
    }
    
    // use onPrepareOptionsMenu instead of onCreateOptionsMenu because we need to recreate the menu
    // each time the button is pressed (as opposed to when this is used with the Action Bar, because
    // in that case the menu always exists after it is initially created
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	showSettings(this);
    	invalidateOptionsMenu();
        return true;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTheme(R.style.AppTheme);
        setContentView(R.layout.main);
        context = this;

        layout = (RelativeLayout)findViewById(R.id.background);
        final Button leftClick = (Button)findViewById(R.id.leftClick);
        final Button rightClick = (Button)findViewById(R.id.rightClick);
        final ImageButton menuClick = (ImageButton)findViewById(R.id.keyboardButton);

        layout.setOnTouchListener(new TouchpadHandler());
        layout.setOnKeyListener(keypadHandler);
        leftClick.setOnClickListener(leftEvent);
        rightClick.setOnClickListener(rightEvent);
        menuClick.setOnClickListener(menuEvent);

        loadPreferences(this);
        Network.LocatorStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        loadPreferences(this);
    }

    public static void loadPreferences(Activity a)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(a.getBaseContext());

        Globals.AutoConnect = prefs.getBoolean(Globals.AUTOCONNECT, true);
        Globals.FirstRun = prefs.getBoolean(Globals.FIRSTRUN, true);
        Globals.Sensitivity = ((float)(prefs.getInt(Globals.SENSITIVITY, 50) + 20)) / 100;
        Globals.Server = prefs.getString(Globals.SERVER, "First");
        Globals.Server_URL = prefs.getString(Globals.SERVER_URL_ID, Globals.Server_URL_Default);

        if (Globals.FirstRun) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Globals.FIRSTRUN, false);
            editor.commit();
        }
    }
}
