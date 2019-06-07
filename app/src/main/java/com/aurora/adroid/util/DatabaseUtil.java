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

import com.aurora.adroid.Constants;
import com.aurora.adroid.model.App;
import com.aurora.adroid.model.Package;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    public static boolean isRepoAvailable(Context context) {
        return PrefUtil.getBoolean(context, Constants.REPO_AVAILABLE);
    }

    public static boolean isDatabaseAvailable(Context context) {
        return PrefUtil.getBoolean(context, Constants.DATABASE_AVAILABLE);
    }

    public static boolean isDatabaseLatest(Context context) {
        return PrefUtil.getBoolean(context, Constants.DATABASE_LATEST);
    }

    public static String getImageUrl(App app) {
        return app.getRepoUrl() + Constants.IMG_URL_PREFIX + app.getIcon();
    }

    public static List<String> getScreenshotURLs(App app) {
        List<String> screenshotFiles = new Gson().fromJson(app.getScreenShots(),
                new TypeToken<List<String>>() {
                }.getType());
        List<String> screenshotUrls = new ArrayList<>();
        if (screenshotFiles != null) {
            for (String fileName : screenshotFiles)
                screenshotUrls.add(app.getRepoUrl()
                        + "/"
                        + app.getPackageName()
                        + "/en-US/phoneScreenshots/"
                        + fileName);
        }
        return screenshotUrls;
    }

    public static String getDownloadURl(App app) {
        return app.getRepoUrl() + "/" + app.getAppPackage().getApkName();
    }

    public static String getDownloadURl(Package pkg) {
        return pkg.getRepoUrl() + "/" + pkg.getApkName();
    }
}