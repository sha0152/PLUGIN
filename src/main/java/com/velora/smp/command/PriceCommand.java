package com.velora.smp.command;

import com.velora.smp.VeloraCore;
import com.velora.smp.util.Chat;
import com.velora.smp.util.MoneyUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public final class PriceCommand implements TabExecutor {
    private final VeloraCore plugin;

    public PriceCommand(VeloraCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getSettings().moduleEnabled("selling")) {
            Chat.send(sender, plugin.getSettings(), "<red>Selling is disabled.</red>");
            return true;
        }
        if (args.length < 1) {
            usage(sender);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "set" -> {
                if (args.length < 3) {
                    usage(sender);
                    return true;
                }
                Material material = Material.matchMaterial(args[1]);
                if (material == null || !material.isItem()) {
                    Chat.send(sender, plugin.getSettings(), "<red>Unknown item.</red>");
                    return true;
                }
                try {
                    var set = plugin.getPriceService().setOverride(material, args[2]);
                    Chat.send(sender, plugin.getSettings(), "<green>Set <item> price to $<amount>.</green>",
                            java.util.Map.of("item", material.name(), "amount", MoneyUtil.format(set, 2)));
                } catch (Exception e) {
                    Chat.send(sender, plugin.getSettings(), "<red>Invalid price.</red>");
                }
                return true;
            }
            case "reset" -> {
                if (args.length < 2) {
                    usage(sender);
                    return true;
                }
                Material material = Material.matchMaterial(args[1]);
                if (material == null || !material.isItem()) {
                    Chat.send(sender, plugin.getSettings(), "<red>Unknown item.</red>");
                    return true;
                }
                plugin.getPriceService().resetOverride(material);
                Chat.send(sender, plugin.getSettings(), "<green>Reset <item> price override.</green>", java.util.Map.of("item", material.name()));
                return true;
            }
            case "list" -> {
                Chat.send(sender, plugin.getSettings(), "<gold>Price overrides:</gold>");
                int i = 0;
                for (var e : plugin.getPriceService().listOverrides().entrySet()) {
                    Chat.send(sender, plugin.getSettings(), "<white>" + e.getKey().name() + "</white>: <green>$" + MoneyUtil.format(e.getValue(), 2) + "</green>");
                    if (++i >= 50) break;
                }
                if (i == 0) {
                    Chat.send(sender, plugin.getSettings(), "<gray>No overrides set.</gray>");
                }
                return true;
            }
            case "reload" -> {
                plugin.reloadSafely();
                Chat.send(sender, plugin.getSettings(), "<green>Reloaded config and prices.</green>");
                return true;
            }
            default -> {
                usage(sender);
                return true;
            }
        }
    }

    private void usage(CommandSender sender) {
        Chat.send(sender, plugin.getSettings(), "<yellow>Usage: /price <set|reset|list|reload> ...</yellow>");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("set", "reset", "list", "reload").stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset"))) {
            String prefix = args[1].toUpperCase();
            List<String> out = new ArrayList<>();
            for (Material m : Material.values()) {
                if (m.isItem() && m.name().startsWith(prefix)) {
                    out.add(m.name());
                    if (out.size() >= 50) break;
                }
            }
            return out;
        }
        return List.of();
    }
}
