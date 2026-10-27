package cz.anderle.fabric.notifications.client.app.model;

import java.util.UUID;

public record Event(UUID id,
                    String title,
                    long time) {
}
