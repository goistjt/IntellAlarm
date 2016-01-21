package com.jrproject.brown_goist.intellalarm.alert;

import android.content.Context;
import android.os.PowerManager;

/**
 * Allows phone to be woken up by alarm from a locked state
 */
public class StaticWakeLock {
    private static PowerManager.WakeLock wl = null;

    public static void lockOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //Object flags;
        if (wl == null)
            wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ALARM");
        wl.acquire();
    }

    public static void lockOff(Context context) {
        if (wl != null)
            wl.release();
    }
}