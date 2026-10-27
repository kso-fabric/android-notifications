package cz.anderle.fabric.notifications.client.app;

import android.content.Context;
import android.util.AtomicFile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cz.anderle.fabric.notifications.client.app.model.Event;

public final class Store {

    private static final String EVENTS_FILE = "events.json";

    private static AtomicFile file(Context c) {
        return new AtomicFile(new File(c.getFilesDir(), EVENTS_FILE));
    }

    public static List<Event> read(Context c) throws Exception {
        AtomicFile f = file(c);
        if (!f.getBaseFile().exists()) {
            return new ArrayList<>();
        }


        var array = new JSONArray(new String(f.readFully(), StandardCharsets.UTF_8));
        var events = new ArrayList<Event>(array.length());

        for (int i = 0; i < array.length(); i++) {
            var item = array.getJSONObject(i);

            events.add(new Event(
                    UUID.fromString(item.getString("id")),
                    item.getString("title"),
                    item.getLong("time")));
        }

        return events;
    }

    public static void write(Context c, List<Event> events) throws Exception {
        var array = new JSONArray();

        for (var event : events) {
            var item = new JSONObject();
            item.put("id", event.id().toString());
            item.put("title", event.title());
            item.put("time", event.time());

            array.put(item);
        }

        AtomicFile f = file(c);
        FileOutputStream out = f.startWrite();
        try {
            out.write(array.toString(2).getBytes(StandardCharsets.UTF_8));
            f.finishWrite(out);
        } catch (Exception e) {
            f.failWrite(out);
            throw e;
        }
    }
}
