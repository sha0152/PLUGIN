package com.velora.smp.command;

import com.velora.smp.VeloraCore;
import com.velora.smp.util.Chat;
import com.velora.smp.util.MoneyUtil;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public final class PricesCommand implements TabExecutor {
    private final VeloraCore plugin;

    public PricesCommand(VeloraCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getSettings().moduleEnabled("selling")) {
            Chat.send(sender, plugin.getSettings(), "<red>Selling is disabled.</red>");
            return true;
        }
        if (args.length == 0) {
            Chat.send(sender, plugin.getSettings(), "<yellow>Usage: /prices <item></yellow>");
            return true;
        }
        Material material = Material.matchMaterial(args[0]);
        if (material == null) {
            Chat.send(sender, plugin.getSettings(), "<red>Unknown item.</red>");
            return true;
        }
        var p = plugin.getPriceService().priceFor(material);
        if (p.isEmpty()) {
            Chat.send(sender, plugin.getSettings(), "<red>That item is excluded from selling.</red>");
            return true;
        }
        Chat.send(sender, plugin.getSettings(), "<gray>Price for " + material.name() + ":</gray> <green>$" + MoneyUtil.format(p.get(), 2) + "</green>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toUpperCase();
            return java.util.Arrays.stream(Material.values())
                    .filter(Material::isItem)
                    .map(Enum::name)
                    .filter(n -> n.startsWith(prefix))
                    .limit(50)
                    .toList();
        }
        return List.of();
    }
}
