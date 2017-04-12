package com.rekap.remote;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

import com.rekap.network.NetInput;

public class KeypadHandler implements OnKeyListener{
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) return false;
		
		switch (event.getAction())
		{
		case KeyEvent.ACTION_UP:
			SendKeyCode(keyCode, (char)event.getUnicodeChar());
			return true;
			
		case KeyEvent.ACTION_MULTIPLE:
			if (keyCode == KeyEvent.KEYCODE_UNKNOWN)
			{
				final String sequence = event.getCharacters();
				new Thread(new Runnable() {
					public void run() {
						NetInput.SendKeySequence(sequence);
					}
				}).start();

			}
			else
			{
				for (int i = 0; i < event.getRepeatCount(); i++)
				{
					SendKeyCode(keyCode, (char)event.getUnicodeChar());
				}
			}
			return true;
		}
		return false;
	}
	
	private void SendKeyCode(int keyCode, final char UnicodeChar)
	{
		switch (keyCode)
		{
		case KeyEvent.KEYCODE_DEL:
			new Thread(new Runnable() {
				public void run() {
					NetInput.SendKeycode(8);
				}
			}).start();
			break;
			
		case KeyEvent.KEYCODE_ENTER:
			new Thread(new Runnable() {
				public void run() {
					NetInput.SendKeycode(13);
				}
			}).start();
			break;
		
		default:
			new Thread(new Runnable() {
				public void run() {
					NetInput.SendKeySequence(new String() + UnicodeChar);
				}
			}).start();
			break;
		}
	}
}
