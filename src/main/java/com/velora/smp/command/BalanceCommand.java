package com.velora.smp.command;

import com.velora.smp.VeloraCore;
import com.velora.smp.util.Chat;
import com.velora.smp.util.MoneyUtil;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class BalanceCommand implements TabExecutor {
    private final VeloraCore plugin;

    public BalanceCommand(VeloraCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getSettings().moduleEnabled("economy")) {
            Chat.send(sender, plugin.getSettings(), "<red>Economy is disabled.</red>");
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                Chat.send(sender, plugin.getSettings(), "<red>Specify a player from console.</red>");
                return true;
            }
            plugin.getEconomyService().getBalance(player.getUniqueId(), player.getName())
                    .whenComplete((bal, err) -> Bukkit.getScheduler().runTask(plugin, () -> {
                        if (err != null) {
                            Chat.send(sender, plugin.getSettings(), "<red>Failed to fetch balance.</red>");
                            return;
                        }
                        Chat.send(sender, plugin.getSettings(), "<gray>Balance:</gray> <green>$<amount></green>",
                                Map.of("amount", MoneyUtil.format(bal, plugin.getEconomyService().scale())));
                    }));
            return true;
        }

        if (!sender.hasPermission("velora.balance.others")) {
            Chat.send(sender, plugin.getSettings(), "<red>You need velora.balance.others.</red>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            Chat.send(sender, plugin.getSettings(), "<red>Player not found online.</red>");
            return true;
        }

        plugin.getEconomyService().getBalance(target.getUniqueId(), target.getName())
                .whenComplete((bal, err) -> Bukkit.getScheduler().runTask(plugin, () -> {
                    if (err != null) {
                        Chat.send(sender, plugin.getSettings(), "<red>Failed to fetch balance.</red>");
                        return;
                    }
                    Chat.send(sender, plugin.getSettings(), "<gray><player>'s balance:</gray> <green>$<amount></green>",
                            Map.of("player", target.getName(), "amount", MoneyUtil.format(bal, plugin.getEconomyService().scale())));
                }));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
