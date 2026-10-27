package cz.anderle.fabric.notifications.client.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

final class Alarms {

    private static final long HOUR = 60L * 60 * 1000;
    private static final long DAY = 24L * HOUR;

    private static final long[] OFFSETS = {
            7L * DAY,
            DAY,
            2 * HOUR,
            HOUR,
            0L
    };

    static boolean allowed(Context c) {
        return Build.VERSION.SDK_INT < 31 || c.getSystemService(AlarmManager.class).canScheduleExactAlarms();
    }

    static PendingIntent intent(Context c, String id, long offset) {
        Uri uri = Uri.parse("fabric.notifications://event/" + id)
                .buildUpon()
                .appendQueryParameter("offset", String.valueOf(offset))
                .build();

        Intent i = new Intent(c, ReminderReceiver.class)
                .setData(uri);

        return PendingIntent.getBroadcast(
                c,
                0,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    static void schedule(Context c, JSONObject item) throws Exception {
        if (!allowed(c)) {
            return;
        }

        long eventTime = item.getLong("time");
        String id = item.getString("id");
        AlarmManager manager = c.getSystemService(AlarmManager.class);

        for (long offset : OFFSETS) {
            long reminderTime = eventTime - offset;

            if (reminderTime > System.currentTimeMillis()) {
                manager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        intent(c, id, offset)
                );
            }
        }
    }

    static void cancel(Context c, String id) {
        AlarmManager manager = c.getSystemService(AlarmManager.class);

        for (long offset : OFFSETS) {
            manager.cancel(intent(c, id, offset));
        }

        // Zrušit také původní alarm vytvořený před touto změnou.
        Intent oldIntent = new Intent(c, ReminderReceiver.class)
                .setData(Uri.parse("fabric.notifications://event/" + id));

        PendingIntent oldPendingIntent = PendingIntent.getBroadcast(
                c,
                0,
                oldIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        manager.cancel(oldPendingIntent);
    }

    static void reschedule(Context c) throws Exception {
        JSONArray items = Store.read(c);

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            cancel(c, item.getString("id"));
            schedule(c, item);
        }
    }
}
