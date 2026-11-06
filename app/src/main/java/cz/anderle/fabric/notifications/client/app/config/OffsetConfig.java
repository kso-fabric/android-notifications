package cz.anderle.fabric.notifications.client.app.config;

import java.util.List;

public class OffsetConfig {

    private static final long HOUR = 60L * 60 * 1000;
    private static final long DAY = 24L * HOUR;

    public static final List<Offset> OFFSETS = List.of(
            new Offset(30L * DAY, "Za měsíc", android.R.drawable.ic_menu_month),
            new Offset(7L * DAY, "Za týden", android.R.drawable.ic_menu_week),
            new Offset(DAY, "Zítra", android.R.drawable.ic_menu_day),
            new Offset(2 * HOUR, "Za dvě hodiny", android.R.drawable.ic_popup_reminder),
            new Offset(HOUR, "Za hodinu", android.R.drawable.ic_lock_idle_alarm),
            new Offset(0L, "Teď", android.R.drawable.sym_def_app_icon)
    );

    /**
     * Find offset by index.
     */
    public static Offset findOffset(String index) {
        if (index != null) {
            var i = Integer.parseInt(index);
            if (i >= 0 && i < OFFSETS.size()) {
                return OFFSETS.get(i);
            }
        }
        return new Offset(0, "Neznámý offset: " + index, android.R.drawable.ic_dialog_alert);
    }

    public record Offset(long value, String description, int icon) {
    }
}
