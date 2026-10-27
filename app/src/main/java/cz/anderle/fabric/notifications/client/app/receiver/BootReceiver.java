package cz.anderle.fabric.notifications.client.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import cz.anderle.fabric.notifications.client.app.Alarms;

/**
 * Je volán po bootu. Nastaví notifikace.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        try {
            Alarms.reschedule(c);
        } catch (Exception e) {
            Log.e("Fabric.Notifications", "Notifications reschedule failed", e);
        }
    }
}
