package me.mapacheee.hitendcrystal.util;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Service
public class MessageUtil {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Inject
    public MessageUtil() {
    }

    public String format(String message) {
        String converted = message.replaceAll("&#([0-9A-Fa-f]{6})", "<#$1>");
        Component component = miniMessage.deserialize(converted);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public Component formatComponent(String message) {
        String converted = message.replaceAll("&#([0-9A-Fa-f]{6})", "<#$1>");
        return miniMessage.deserialize(converted);
    }
}
