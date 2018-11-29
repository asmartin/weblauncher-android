/*
    WOLActivity.java - main activity of Simple Wake On Lan application.

    Copyright (C) 2018  Maxim Belyaev(NFS_MONSTR)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tk.nfsmonstr.simplewakeonlan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.avidandrew.weblauncher.R;
import com.rekap.remote.Globals;

import java.util.ArrayList;

public class WOLActivity extends AppCompatActivity {
    ListView servers_list = null;
    ServerListAdapter serverListAdapter = null;
    Activity mActivity = this;
    ArrayList<String> name, ip, mac;
    ArrayList<Integer> port;
    ArrayList<Boolean> broadcast;

    private void setupServerList() {
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE);
        int serversCount = sharedPreferences.getInt("serversCount",0);
        name = new ArrayList<>();
        ip = new ArrayList<>();
        mac = new ArrayList<>();
        port = new ArrayList<>();
        broadcast = new ArrayList<>();
        for (int i = 0;i<serversCount;i++) {
            name.add(sharedPreferences.getString("server-name-".concat(String.valueOf(i)),""));
            ip.add(sharedPreferences.getString("server-ip-".concat(String.valueOf(i)),""));
            mac.add(sharedPreferences.getString("server-mac-".concat(String.valueOf(i)),""));
            port.add(sharedPreferences.getInt("server-port-".concat(String.valueOf(i)),0));
            broadcast.add(sharedPreferences.getBoolean("server-broad-".concat(String.valueOf(i)),false));
        }
        serverListAdapter = new ServerListAdapter(this,name,ip,port,mac,broadcast);
        servers_list.setAdapter(serverListAdapter);
        servers_list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Globals.WOL_Server = name.get(position);
                new SendMagicPacket(mActivity,ip.get(position),mac.get(position),port.get(position),broadcast.get(position)).execute();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        servers_list = findViewById(R.id.server_list);
        setupServerList();
        FloatingActionButton addServer = findViewById(R.id.addServer);
        addServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, AddOrEditServerActivity.class);
                intent.putExtra("EXTRA_MODE", 0);
                startActivity(intent);
            }
        });
        registerForContextMenu(servers_list);
    }

    @Override
    protected void onResume() {
        setupServerList();
        super.onResume();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.servers, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_edit:
                int number = info.position;
                Intent intent = new Intent(mActivity, AddOrEditServerActivity.class);
                intent.putExtra("EXTRA_NUMBER", number);
                intent.putExtra("EXTRA_MODE", 1);
                startActivity(intent);
                setupServerList();
                return true;
            case R.id.menu_del:
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.settings), Context.MODE_PRIVATE);
                int count = sharedPreferences.getInt("serversCount",0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                for (int i=info.position;i<count-1;i++) {
                    editor.remove("server-name-".concat(String.valueOf(i)));
                    editor.remove("server-ip-".concat(String.valueOf(i)));
                    editor.remove("server-mac-".concat(String.valueOf(i)));
                    editor.remove("server-port-".concat(String.valueOf(i)));
                    editor.remove("server-broad-".concat(String.valueOf(i)));
                    editor.putString("server-name-".concat(String.valueOf(i)),sharedPreferences.getString("server-name-".concat(String.valueOf(i+1)),""));
                    editor.putString("server-ip-".concat(String.valueOf(i)),sharedPreferences.getString("server-ip-".concat(String.valueOf(i+1)),""));
                    editor.putString("server-mac-".concat(String.valueOf(i)),sharedPreferences.getString("server-mac-".concat(String.valueOf(i+1)),""));
                    editor.putInt("server-port-".concat(String.valueOf(i)),sharedPreferences.getInt("server-port-".concat(String.valueOf(i+1)),0));
                    editor.putBoolean("server-broad-".concat(String.valueOf(i)),sharedPreferences.getBoolean("server-broad-".concat(String.valueOf(i+1)),false));
                }
                count--;
                editor.putInt("serversCount",count);
                editor.apply();
                setupServerList();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    private class ServerListAdapter extends ArrayAdapter<String> {
        Activity mActivity;
        ArrayList<String> name, ip, mac;
        ArrayList<Integer> port;
        ArrayList<Boolean> broadcast;

        private ServerListAdapter(Activity context,ArrayList<String> name, ArrayList<String> ip,ArrayList<Integer> port,ArrayList<String> mac, ArrayList<Boolean> broadcast) {
            super(context, R.layout.server_single, name);
            mActivity = context;
            this.name = name;
            this.ip = ip;
            this.port = port;
            this.mac = mac;
            this.broadcast = broadcast;
        }

        @NonNull
        @Override
        public View getView(int position, View view,@NonNull ViewGroup parent) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            final View rowView= inflater.inflate(R.layout.server_single, null, true);
            TextView nameView = rowView.findViewById(R.id.server_name);
            nameView.setText(name.get(position));
            TextView ipView = rowView.findViewById(R.id.server_ip);
            ipView.setText(ip.get(position).concat(":".concat(String.valueOf(port.get(position)))));
            TextView macView = rowView.findViewById(R.id.server_mac);
            macView.setText(mac.get(position));
            return rowView;
        }
    }
}
