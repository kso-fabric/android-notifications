package cz.anderle.fabric.notifications.client.app;

import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.*;

import cz.anderle.fabric.notifications.client.app.model.Event;

public class MainActivity extends Activity {
    private EditText title;
    private TextView selected, warning;
    private LinearLayout list;
    private final Calendar date = Calendar.getInstance();

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        date.add(Calendar.HOUR_OF_DAY, 1);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        setContentView(R.layout.activity_main);
        title = findViewById(R.id.eventTitle);
        selected = findViewById(R.id.selectedDate);
        warning = findViewById(R.id.warning);
        list = findViewById(R.id.eventList);
        Button day = findViewById(R.id.dateButton);
        Button time = findViewById(R.id.timeButton);
        Button add = findViewById(R.id.saveButton);
        updateDate();

        day.setOnClickListener(v -> new DatePickerDialog(this, (p, y, m, d) -> {
            date.set(y, m, d);
            updateDate();
        }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show());
        time.setOnClickListener(v -> new TimePickerDialog(this, (p, h, m) -> {
            date.set(Calendar.HOUR_OF_DAY, h);
            date.set(Calendar.MINUTE, m);
            updateDate();
        }, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), true).show());
        add.setOnClickListener(v -> add());
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (list != null) {
            refresh();
            updateWarning();
            try {
                Alarms.reschedule(this);
            } catch (Exception e) {
                error(e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int request, String[] permissions, int[] grants) {
        super.onRequestPermissionsResult(request, permissions, grants);
        updateWarning();
    }

    private void updateDate() {
        selected.setText(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date.getTime()));
    }

    private void updateWarning() {
        boolean notifications = Build.VERSION.SDK_INT < 33 || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        if (!notifications || !getSystemService(NotificationManager.class).areNotificationsEnabled()) {
            warning.setText("Oznámení jsou vypnutá. Klepnutím otevřeš nastavení oznámení.");
            warning.setVisibility(View.VISIBLE);
            warning.setOnClickListener(v -> {
                Intent i = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(i);
            });
        } else if (Alarms.notAllowed(this)) {
            warning.setText("Pro včasné připomenutí povol přesná upozornění. Klepnutím otevřeš nastavení.");
            warning.setVisibility(View.VISIBLE);
            warning.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= 31)
                    startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName())));
            });
        } else warning.setVisibility(View.GONE);
    }

    private void add() {
        String text = title.getText().toString().trim();
        if (text.isEmpty()) {
            title.setError("Zadej název události");
            return;
        }
        if (date.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(this, "Vyber čas v budoucnosti", Toast.LENGTH_LONG).show();
            return;
        }
        if (Alarms.notAllowed(this)) {
            updateWarning();
            Toast.makeText(this, "Nejprve povol přesná upozornění", Toast.LENGTH_LONG).show();
            return;
        }
        if (!getSystemService(NotificationManager.class).areNotificationsEnabled() || (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) {
            updateWarning();
            Toast.makeText(this, "Nejprve povol oznámení", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            var events = Store.read(this);
            var event = new Event(
                    UUID.randomUUID(),
                    text,
                    date.getTimeInMillis());
            events.add(event);

            Store.write(this, events);
            Alarms.schedule(this, event);
            title.setText("");
            refresh();
            Toast.makeText(this, "Připomínka uložena", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            error(e);
        }
    }

    private void refresh() {
        list.removeAllViews();
        try {
            var events = Store.read(this);
            events.sort(Comparator.comparingLong(Event::time));

            if (events.isEmpty()) {
                TextView empty = new TextView(this);
                empty.setText("Zatím tu nic není.");
                list.addView(empty);
            }

            for (var event : events) {
                View row = LayoutInflater.from(this).inflate(R.layout.item_event, list, false);
                TextView label = row.findViewById(R.id.eventLabel);
                Button delete = row.findViewById(R.id.deleteButton);
                long when = event.time();

                label.setText(event.title() + "\n"
                        + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(when))
                        + (when <= System.currentTimeMillis() ? " · proběhlo" : ""));
                delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                        .setMessage("Smazat tuto událost?")
                        .setNegativeButton("Zrušit", null)
                        .setPositiveButton("Smazat", (d, w) -> remove(event))
                        .show());
                list.addView(row);
            }
        } catch (Exception e) {
            error(e);
        }
    }

    private void remove(Event event) {
        try {
            var events = Store.read(this);

            events.remove(event);
            Store.write(this, events);
            Alarms.cancel(this, event);
            getSystemService(NotificationManager.class).cancel(event.id().hashCode());
            refresh();
        } catch (Exception e) {
            error(e);
        }
    }

    private void error(Exception e) {
        new AlertDialog.Builder(this)
                .setTitle("Něco se nepovedlo")
                .setMessage("Data se nepodařilo načíst nebo uložit: " + e.getMessage())
                .setPositiveButton("OK", null)
                .show();
    }
}
