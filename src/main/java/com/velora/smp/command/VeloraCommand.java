package com.velora.smp.command;

import com.velora.smp.VeloraCore;
import com.velora.smp.util.Chat;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public final class VeloraCommand implements TabExecutor {
    private final VeloraCore plugin;

    public VeloraCommand(VeloraCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                plugin.reloadSafely();
                Chat.send(sender, plugin.getSettings(), "<green>VeloraCore reloaded safely.</green>");
            }
            case "backup" -> {
                Chat.send(sender, plugin.getSettings(), "<yellow>Backup started...</yellow>");
                plugin.getDatabaseManager().backupNow().whenComplete((path, err) -> Bukkit.getScheduler().runTask(plugin, () -> {
                    if (err != null) {
                        Chat.send(sender, plugin.getSettings(), "<red>Backup failed: " + err.getMessage() + "</red>");
                        return;
                    }
                    Chat.send(sender, plugin.getSettings(), "<green>Backup saved to <path>.</green>", Map.of("path", path.toString()));
                }));
            }
            case "info" -> {
                Chat.send(sender, plugin.getSettings(), "<gold>" + plugin.getSettings().displayName() + "</gold> <gray>v" + plugin.getPluginMeta().getVersion() + "</gray>");
                Chat.send(sender, plugin.getSettings(), "<gray>Modules: economy=" + plugin.getSettings().moduleEnabled("economy")
                        + ", selling=" + plugin.getSettings().moduleEnabled("selling")
                        + ", auction=" + plugin.getSettings().moduleEnabled("auction") + "</gray>");
                Chat.send(sender, plugin.getSettings(), "<gray>Universal material pricing healthy: " + plugin.getPriceService().allValidMaterialsHavePrice() + "</gray>");
            }
            default -> help(sender);
        }
        return true;
    }

    private void help(CommandSender sender) {
        Chat.send(sender, plugin.getSettings(), "<gold>/velora reload</gold> <gray>- reload plugin configs</gray>");
        Chat.send(sender, plugin.getSettings(), "<gold>/velora backup</gold> <gray>- create sqlite backup</gray>");
        Chat.send(sender, plugin.getSettings(), "<gold>/velora info</gold> <gray>- show plugin status</gray>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "backup", "info").stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
