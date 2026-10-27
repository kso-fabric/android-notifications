package cz.anderle.fabric.notifications.client.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.util.UUID;

import cz.anderle.fabric.notifications.client.app.config.OffsetConfig;
import cz.anderle.fabric.notifications.client.app.config.UriConfig;
import cz.anderle.fabric.notifications.client.app.model.Event;
import cz.anderle.fabric.notifications.client.app.receiver.ReminderReceiver;

public final class Alarms {

    public static boolean notAllowed(Context c) {
        return Build.VERSION.SDK_INT >= 31 && !c.getSystemService(AlarmManager.class).canScheduleExactAlarms();
    }

    public static PendingIntent intent(Context c, UUID id, int offsetIndex) {
        Uri uri = Uri.parse(UriConfig.URI + id)
                .buildUpon()
                .appendQueryParameter(UriConfig.OFFSET, String.valueOf(offsetIndex))
                .build();

        Intent i = new Intent(c, ReminderReceiver.class)
                .setData(uri);

        return PendingIntent.getBroadcast(c,0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void schedule(Context c, Event event) {
        if (notAllowed(c)) {
            return;
        }

        AlarmManager manager = c.getSystemService(AlarmManager.class);

        for (int i = 0; i < OffsetConfig.OFFSETS.size(); i++) {
            var offset = OffsetConfig.OFFSETS.get(i);
            long reminderTime = event.time() - offset.value();

            if (reminderTime > System.currentTimeMillis()) {
                manager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        intent(c, event.id(), i)
                );
            }
        }
    }

    public static void cancel(Context c, Event event) {
        AlarmManager manager = c.getSystemService(AlarmManager.class);

        for (int i = 0; i < OffsetConfig.OFFSETS.size(); i++) {
            manager.cancel(intent(c, event.id(), i));
        }
    }

    public static void reschedule(Context c) throws Exception {
        for (var event : Store.read(c)) {
            cancel(c, event);
            schedule(c, event);
        }
    }
}
