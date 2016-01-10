/* Copyright 2014 Sheldon Neilson www.neilson.co.za
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.jrproject.brown_goist.intellalarm.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.jrproject.brown_goist.intellalarm.Alarm;
import com.jrproject.brown_goist.intellalarm.AlarmActivity;
import com.jrproject.brown_goist.intellalarm.alert.AlarmAlertBroadcastReceiver;
import com.jrproject.brown_goist.intellalarm.database.AlarmDatabase;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AlarmService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(this.getClass().getSimpleName(), "onCreate()");
        super.onCreate();
    }

    private Alarm getNext() {
        Set<Alarm> alarmQueue = new TreeSet<>(new Comparator<Alarm>() {
            @Override
            public int compare(Alarm lhs, Alarm rhs) {
                long diff = lhs.getAlarmTime().getTimeInMillis() - rhs.getAlarmTime().getTimeInMillis();
                return diff > 0 ? 1 : diff < 0 ? -1 : 0;
            }
        });
        AlarmDatabase.init(getApplicationContext());
        List<Alarm> alarms = AlarmDatabase.getAll();

        for (Alarm alarm : alarms) {
            if (alarm.getAlarmActive())
                alarmQueue.add(alarm);
        }
        if (alarmQueue.iterator().hasNext()) {
            return alarmQueue.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public void onDestroy() {
        AlarmDatabase.deactivate();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(this.getClass().getSimpleName(), "onStartCommand()");
        Alarm alarm = getNext();
        if (alarm != null) {
            alarm.schedule(getApplicationContext());
            Log.d(this.getClass().getSimpleName(), alarm.getTimeUntilNextAlarmMessage());
        } else {
            Intent myIntent = new Intent(getApplicationContext(), AlarmAlertBroadcastReceiver.class);
            myIntent.putExtra("alarm", new Alarm());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }
        return START_NOT_STICKY;
    }

}
