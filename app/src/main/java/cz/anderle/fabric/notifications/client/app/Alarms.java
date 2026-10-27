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
    static boolean allowed(Context c) {
        return Build.VERSION.SDK_INT < 31 || c.getSystemService(AlarmManager.class).canScheduleExactAlarms();
    }
    static PendingIntent intent(Context c, String id) {
        Intent i = new Intent(c, ReminderReceiver.class).setData(Uri.parse("fabric.notifications://event/" + id));
        return PendingIntent.getBroadcast(c, 0, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    static void schedule(Context c, JSONObject item) throws Exception {
        if (!allowed(c)) return;
        long when = item.getLong("time");
        if (when > System.currentTimeMillis()) c.getSystemService(AlarmManager.class).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, intent(c, item.getString("id")));
    }
    static void cancel(Context c, String id) { c.getSystemService(AlarmManager.class).cancel(intent(c, id)); }
    static void reschedule(Context c) throws Exception {
        JSONArray a = Store.read(c);
        for (int i = 0; i < a.length(); i++) schedule(c, a.getJSONObject(i));
    }
}
