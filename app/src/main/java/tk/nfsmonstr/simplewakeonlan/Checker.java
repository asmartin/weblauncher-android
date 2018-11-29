/*
    Checker.java - part of Simple Wake On Lan application, that's allow to check correctness of entered ip or mac address.

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

import java.util.regex.Pattern;

/**
 * Created by nfs_monstr on 06.01.18.
 */

public class Checker {
    private static final String IPV4_PATTERN_STRING = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
    private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_PATTERN_STRING);

    private static final String MAC_PATTERN_STRING  = "[0-9a-fA-F]{2}-[0-9a-fA-F]{2}-[0-9a-fA-F]{2}-[0-9a-fA-F]{2}-[0-9a-fA-F]{2}-[0-9a-fA-F]{2}";
    private static final String MAC_PATTERN_STRING2 = "[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}:[0-9a-fA-F]{2}";
    private static final Pattern MAC_PATTERN  = Pattern.compile(MAC_PATTERN_STRING);
    private static final Pattern MAC_PATTERN2 = Pattern.compile(MAC_PATTERN_STRING2);

    public static boolean checkIp(String ip) {
        if (ip == null)
            return false;
        return IPV4_PATTERN.matcher(ip).matches();
    }

    public static boolean checkMac(String mac) {
        if (mac == null)
            return false;
        return MAC_PATTERN.matcher(mac).matches() || MAC_PATTERN2.matcher(mac).matches();
    }


}
