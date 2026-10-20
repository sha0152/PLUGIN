package com.velora.smp.command;

import com.velora.smp.VeloraCore;
import com.velora.smp.util.Chat;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public final class ModuleAwareInfoCommand implements TabExecutor {
    private final VeloraCore plugin;
    private final String module;
    private final String message;

    public ModuleAwareInfoCommand(VeloraCore plugin, String module, String message) {
        this.plugin = plugin;
        this.module = module;
        this.message = message;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getSettings().moduleEnabled(module)) {
            Chat.send(sender, plugin.getSettings(), "<red>This module is disabled.</red>");
            return true;
        }
        Chat.send(sender, plugin.getSettings(), "<yellow>" + message + "</yellow>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
