/*
 * Copyright (c) 2016 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metinkale.prayerapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.metinkale.prayer.BuildConfig;
import com.metinkale.prayerapp.vakit.WidgetService;
import com.metinkale.prayerapp.vakit.sounds.Sounds;
import com.metinkale.prayerapp.vakit.times.MainHelper;
import com.metinkale.prayerapp.vakit.times.Times;
import io.fabric.sdk.android.Fabric;
import net.danlew.android.joda.JodaTimeAndroid;


public class App extends Application {
    public static final String API_URL = "http://metinkale38.github.io/namaz-vakti-android/files";
    private static Context sContext;
    private static Handler sHandler = new Handler();
    private static Tracker sTracker;


    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;

        Fabric.with(this, new Crashlytics());

        JodaTimeAndroid.init(this);
        MainHelper.copy();
        Times.getTimes();
        Utils.init();

        startService(new Intent(this, WidgetService.class));
        MainIntentService.setAlarms(this);
    }

    public static void setContext(Context context) {
        sContext = context;
    }

    public static Context getContext() {
        return sContext;
    }


    public static boolean isOnline() {
        if (Sounds.needsCheck()) new Thread(new Runnable() {
            @Override
            public void run() {
                Sounds.checkIfNeeded();

            }
        }).start();

        ConnectivityManager conMgr = (ConnectivityManager) sContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }


    @SuppressLint("NewApi")
    public static void setExact(AlarmManager am, int type, long time, PendingIntent service) {
        if (Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(type, time, service);
        } else if (Build.VERSION.SDK_INT >= 19) {
            am.setExact(type, time, service);
        } else {
            am.set(type, time, service);
        }

    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public static Tracker getTracker() {
        if (sTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(getContext());
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            sTracker = analytics.newTracker("UA-79170076-1");
        }
        return sTracker;
    }

    public static Handler getHandler() {
        return sHandler;
    }


    @Override
    public void onTerminate() {
        sTracker = null;
        sHandler = null;
        sContext = null;
        super.onTerminate();
    }


    public static final class NotIds {
        public static final int ALARM = 1;
        public static final int ONGOING = 2;
    }


}