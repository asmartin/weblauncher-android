package tk.nfsmonstr.simplewakeonlan;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import com.avidandrew.weblauncher.R;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import android.net.wifi.WifiManager;
import android.net.DhcpInfo;
import com.rekap.remote.Globals;

/**
 * Created by nfs_monstr on 07.01.18.
 */

public class SendMagicPacket extends AsyncTask<Void,Void,Byte> {
    private Context context;
    private String ip;
    private String mac;
    private int port;
    private boolean broadcast;

    private String msg = "";

    SendMagicPacket(Context context,String ip, String mac, int port, boolean broadcast) {
        this.context = context;
        this.ip = ip;
        this.mac = mac.toUpperCase().replaceAll(":","").replaceAll("-","");
        this.port = port;
        this.broadcast = broadcast;
        if (Globals.DEBUG)
            Log.d("WebLauncher", "ip: "+ this.ip + "; mac: " + this.mac + "; port: " + this.port + "; broadcast: " + this.broadcast);
    }

    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager myWifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
        DhcpInfo myDhcpInfo = myWifiManager.getDhcpInfo();
        if (myDhcpInfo == null) {
            // Could not get broadcast address
            return null;
        }
        int broadcast = (myDhcpInfo.ipAddress & myDhcpInfo.netmask)
                | ~myDhcpInfo.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    @Override
    protected Byte doInBackground(Void... params) {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            byte[] buffer = new byte[102];
            byte[] macBuff = new byte[6];
            for (int i=0;i<6;i++) {
                buffer[i] = (byte) 0xFF;
                macBuff[i] = Converter.hexIntoByte(mac.substring(i*2,i*2+2));
            }
            for (int i=0; i<16; i++) {
                System.arraycopy(macBuff,0,buffer,6+6*i,6);
            }

            // send to IP address
            InetAddress inetAddress = InetAddress.getByName(ip);
            datagramSocket.connect(inetAddress,port);
            DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length,inetAddress,port);
            datagramSocket.send(datagramPacket);
            datagramSocket.close();

            // send to broadcast address
            if (broadcast) {
                InetAddress bcast = getBroadcastAddress();
                if (bcast == null) {
                    throw new Exception("cannot get broadcast address");
                }
                datagramSocket = new DatagramSocket();
                datagramSocket.setBroadcast(broadcast);
                datagramSocket.connect(bcast, port);
                datagramPacket = new DatagramPacket(buffer, buffer.length, bcast, port);
                datagramSocket.send(datagramPacket);
                datagramSocket.close();
            }
        } catch (Exception e) {
            msg = e.getMessage();
            if (Globals.DEBUG)
              Log.d("WebLauncher", "SendMagicPacket: " + msg);
            return 1;
        }
        return 0;
    }

    @Override
    protected void onPostExecute(Byte result) {
        if (result==1) {
            Toast.makeText(context,context.getString(R.string.send_fail).concat(" ".concat(msg)),Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context,context.getString(R.string.send_ok),Toast.LENGTH_SHORT).show();
        }
    }
}
