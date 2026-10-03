package com.pranav.saarthi.health;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Re-registers periodic health checks after device reboot.
 */
public class HealthCheckBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        HealthCheckScheduler.schedule(context.getApplicationContext());
    }
}
