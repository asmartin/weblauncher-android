package com.protoscratch.locator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.rekap.remote.Globals;


public class LocatorClient extends Thread{
	
	private int Port;
	private byte[] Key;
	private DatagramSocket dSocket;
	private Map<String, String[]> servers;
	private Timer detectTimer;
	private long STALE_TIME = 5;	// seconds until a server entry is considered stale (should be removed)
	
	TimerTask detectTask = new TimerTask() {
		@Override
		public void run() {
			Detect();
		}
	};
	
	public LocatorClient(int Port, byte[] Key)
	{
		this.Port = Port;
		this.Key = Key;
		servers = new HashMap<String, String[]>();
	}
	
	/** 
	 * returns the epoch time in seconds
	 * @return the epoch time in seconds
	 */
	private long getNow() {
		return System.currentTimeMillis() / 1000;
	}
	
	public void run()
	{
        if (detectTimer == null)
        {
        	detectTimer = new Timer();
        	detectTimer.schedule(detectTask, 5000, 5000);
        }
        
		try {
			byte[] buff = new byte[1024];
			dSocket = new DatagramSocket();
			DatagramPacket dPacket = new DatagramPacket(buff, buff.length);
			Detect();
			
			while (!interrupted())
			{
				dSocket.receive(dPacket);
				String address = dPacket.getAddress().getHostAddress();
				if (dPacket.getLength() > 4 && buff[0] == Key[0] && buff[1] == Key[1] && buff[2] == Key[2] && buff[3] == Key[3])
				{
					byte[] data = new byte[dPacket.getLength() - 4];
					System.arraycopy(buff, 4, data, 0, data.length);
					processFrame(address, data);
				}
			}
		} catch (IOException e) { }
		dSocket = null;
	}
	
	public synchronized String GetServerAddress(String Name)
	{
		try {
			return servers.get(Name)[0];
		} catch (NullPointerException npe) {
			// no mapping found
			return null;
		}
	}

	/**
	 * returns the time that this entry was added to the list
	 * @param Name key of the entry
	 * @return epoch time in seconds when this entry was added
	 */
	public synchronized long GetServerTime(String Name)
	{
		try {
			return Long.valueOf(servers.get(Name)[1]);
		} catch (NumberFormatException nfe) {
			// return a stale time to get this entry cleared out
			return getNow() - (STALE_TIME * 2);
		}
	}
	
	public synchronized String[] GetServerNames()
	{
		return servers.keySet().toArray(new String[servers.keySet().size()]);
	}
	
	private void processFrame(String address, byte[] data)
	{
		try {
			// remove any existing stale entries
			long now = getNow();
			String[] names = GetServerNames();
		//	boolean resetGlobal = false;
			
			for (String name : names) {
				if (now - GetServerTime(name) > STALE_TIME) {
					// entry is stale, remove it
					Globals.Debugger("Locator", "removing stale: " + name);
					servers.remove(name);
					
					/*if (Globals.Server.equals(name)) {
						// we need to update the global server since it was using the now stale server
						resetGlobal = true;
					}*/
				}
			}
			
			// refresh the list
			names = GetServerNames();
			for (String name : names) {
				Globals.Debugger("Locator", "valid server: " + name);
			}
			Globals.Debugger("Locator", "----------");
			/*
			// if we need to update the Globals.Server preference, then do so
			if (resetGlobal && names != null && names.length > 0) {
				Globals.Server = names[0];
			}*/
			
			// add this new entry
			ByteBuffer bbData = ByteBuffer.wrap(data);
			bbData.order(ByteOrder.BIG_ENDIAN);
			byte[] rawName = new byte[bbData.getShort()];
			bbData.get(rawName, 0, rawName.length);
			String name = new String(rawName, "UTF16");
			//if (!servers.containsKey("First"))
			//	servers.put("First", new String[]{ address, Long.toString(getNow()) });
			servers.put(name, new String[]{ address, Long.toString(getNow()) });
		} catch (UnsupportedEncodingException e) { } //should never happen
	}
	
	public synchronized void Detect()
	{
		if (dSocket == null) return;
		try {
			DatagramPacket sPacket = new DatagramPacket(Key, Key.length);
			sPacket.setPort(Port);
			sPacket.setAddress(InetAddress.getByAddress(new byte[] { (byte)255, (byte)255, (byte)255, (byte)255 }));
			dSocket.send(sPacket);
		} catch (UnknownHostException e) { } catch (IOException e) { }
	}
}
