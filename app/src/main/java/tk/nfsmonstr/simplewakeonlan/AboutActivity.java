/*
    AboutActivity.java - about activity of Simple Wake On Lan application.

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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Button;
import com.avidandrew.weblauncher.R;
import com.rekap.remote.Globals;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView version = (TextView)findViewById(R.id.versionText);
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText("Version: " + pinfo.versionName);
        } catch(NameNotFoundException exp) {
            if (Globals.DEBUG)
                Log.d("WebLauncher", "could not read package name from AndroidManifest.xml");
        }
        Button showLicense = (Button)findViewById(R.id.showLicense);
        showLicense.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://www.gnu.org/licenses"));
                startActivity(browserIntent);
            }
        });
    }
}
