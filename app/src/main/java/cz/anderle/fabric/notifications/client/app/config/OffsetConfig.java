package cz.anderle.fabric.notifications.client.app.config;

import java.util.List;

public class OffsetConfig {

    private static final long HOUR = 60L * 60 * 1000;
    private static final long DAY = 24L * HOUR;

    public static final List<Offset> OFFSETS = List.of(
            new Offset(7L * DAY, "Za týden"),
            new Offset(DAY, "Zítra"),
            new Offset(2 * HOUR, "Za dvě hodiny"),
            new Offset(HOUR, "Za hodinu"),
            new Offset(0L, "Teď")
    );

    public static String findOffset(String index) {
        if (index != null) {
            var i = Integer.parseInt(index);
            if (i >= 0 && i < OFFSETS.size()) {
                return OFFSETS.get(i).description();
            }
        }
        return "Neznámý offset";
    }

    public record Offset(long value, String description) {
    }
}
