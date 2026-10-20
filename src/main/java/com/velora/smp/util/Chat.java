package com.velora.smp.util;

import com.velora.smp.config.Settings;
import java.util.Map;
import org.bukkit.command.CommandSender;

public final class Chat {
    private Chat() {}

    public static void send(CommandSender sender, Settings settings, String template) {
        sender.sendMessage(settings.prefix() + template);
    }

    public static void send(CommandSender sender, Settings settings, String template, Map<String, String> placeholders) {
        String value = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            value = value.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        sender.sendMessage(settings.prefix() + value);
    }
}
