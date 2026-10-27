package cz.anderle.fabric.notifications.client.app;

import android.content.Context;
import android.util.AtomicFile;

import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

final class Store {
    private static AtomicFile file(Context c) {
        return new AtomicFile(new File(c.getFilesDir(), "events.json"));
    }

    static JSONArray read(Context c) throws Exception {
        AtomicFile f = file(c);
        if (!f.getBaseFile().exists()) return new JSONArray();
        return new JSONArray(new String(f.readFully(), StandardCharsets.UTF_8));
    }

    static void write(Context c, JSONArray data) throws Exception {
        AtomicFile f = file(c);
        FileOutputStream out = f.startWrite();
        try {
            out.write(data.toString(2).getBytes(StandardCharsets.UTF_8));
            f.finishWrite(out);
        } catch (Exception e) {
            f.failWrite(out);
            throw e;
        }
    }
}
