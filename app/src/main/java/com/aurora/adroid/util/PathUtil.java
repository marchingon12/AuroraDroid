/*
 * Aurora Droid
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Aurora Droid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora Droid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Droid.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aurora.adroid.util;

import android.content.Context;
import android.os.Environment;

import com.aurora.adroid.Constants;

import java.io.File;

public class PathUtil {

    public static String getRepoDirectory(Context context) {
        final File dir = new File(context.getFilesDir().getPath() + "/repositories/");
        if (!dir.exists())
            dir.mkdir();
        return dir.getPath() + "/";
    }

    static public String getRootApkPath(Context context) {
        if (isCustomPath(context))
            return PrefUtil.getString(context, Constants.PREFERENCE_DOWNLOAD_DIRECTORY);
        else
            return getBaseApkDirectory(context);
    }

    public static String getBaseApkDirectory(Context context) {
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P *//*&& Util.isRootInstallEnabled(context)*//*) {
            return context.getFilesDir().getPath();
        } else*/
            return Environment.getExternalStorageDirectory().getPath() + "/AuroraDroid";
    }

    public static String getApkPath(Context context, String apkName) {
        return getRootApkPath(context) + "/" + apkName;
    }

    public static boolean fileExists(Context context, String fileName) {
        return new File(getApkPath(context, fileName)).exists();
    }

    public static synchronized void deleteFile(Context context, String fileName) {
        for (File file : new File(getRepoDirectory(context)).listFiles())
            if (file.getName().startsWith(fileName))
                file.delete();
    }

    static private boolean isCustomPath(Context context) {
        return (!getCustomPath(context).isEmpty());
    }

    static public String getCustomPath(Context context) {
        return PrefUtil.getString(context, Constants.PREFERENCE_DOWNLOAD_DIRECTORY);
    }

    static public String getBaseCopyDirectory() {
        return Environment.getExternalStorageDirectory().getPath() + "/Aurora/Copy/APK";
    }
}