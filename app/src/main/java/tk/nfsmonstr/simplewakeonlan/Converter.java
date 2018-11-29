/*
    Converter.java - part of Simple Wake On Lan application, that's allow to convert hex string into byte.

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

/**
 * Created by nfs_monstr on 07.01.18.
 */

public class Converter {

    public static byte hexIntoByte(String hex) {
        byte result = 0;
        StringBuilder sb = new StringBuilder(hex);
        int len = Math.min(2,sb.length());
        for (int i=0;i<len;i++) {
            result<<=4;
            if ((sb.charAt(0) >= '0') && (sb.charAt(0) <= '9')) {
                result |= (byte) (((byte) sb.charAt(0) - (byte) '0'));
            } else {
                result |= (byte) (((byte) sb.charAt(0) - (byte) 'A' + 10));
            }
            sb.deleteCharAt(0);
        }
        return  result;
    }
}
