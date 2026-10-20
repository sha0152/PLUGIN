package com.velora.smp.command;

import com.velora.smp.VeloraCore;
import com.velora.smp.economy.EconomyService;
import com.velora.smp.util.Chat;
import com.velora.smp.util.MoneyUtil;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public final class BaltopCommand implements TabExecutor {
    private final VeloraCore plugin;

    public BaltopCommand(VeloraCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getSettings().moduleEnabled("balance-leaderboard")) {
            Chat.send(sender, plugin.getSettings(), "<red>Balance leaderboard is disabled.</red>");
            return true;
        }
        int page = 1;
        if (args.length > 0) {
            try {
                page = Math.max(1, Integer.parseInt(args[0]));
            } catch (NumberFormatException ignored) {
            }
        }

        final int pageFinal = page;
        plugin.getEconomyService().top(pageFinal, 10).whenComplete((entries, err) -> Bukkit.getScheduler().runTask(plugin, () -> {
            if (err != null) {
                Chat.send(sender, plugin.getSettings(), "<red>Failed to load leaderboard.</red>");
                return;
            }
            Chat.send(sender, plugin.getSettings(), "<gold>Top balances (page " + pageFinal + "):</gold>");
            int[] idx = {1 + (pageFinal - 1) * 10};
            for (EconomyService.BaltopEntry entry : entries) {
                Chat.send(sender, plugin.getSettings(), "<gray>#" + idx[0] + "</gray> <white>" + entry.name() + "</white> <green>$"
                        + MoneyUtil.format(entry.balance(), plugin.getEconomyService().scale()) + "</green>");
                idx[0]++;
            }
        }));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
