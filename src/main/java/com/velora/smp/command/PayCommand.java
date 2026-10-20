package com.velora.smp.command;

import com.velora.smp.VeloraCore;
import com.velora.smp.util.Chat;
import com.velora.smp.util.MoneyUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public final class PayCommand implements TabExecutor {
    private final VeloraCore plugin;

    public PayCommand(VeloraCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getSettings().moduleEnabled("economy")) {
            Chat.send(sender, plugin.getSettings(), "<red>Economy is disabled.</red>");
            return true;
        }
        if (!(sender instanceof Player player)) {
            Chat.send(sender, plugin.getSettings(), "<red>Only players can pay.</red>");
            return true;
        }
        if (args.length < 2) {
            Chat.send(sender, plugin.getSettings(), "<yellow>Usage: /pay <player> <amount></yellow>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            Chat.send(sender, plugin.getSettings(), "<red>Player not found online.</red>");
            return true;
        }
        if (!plugin.getSettings().allowSelfPay() && target.getUniqueId().equals(player.getUniqueId())) {
            Chat.send(sender, plugin.getSettings(), "<red>You cannot pay yourself.</red>");
            return true;
        }

        BigDecimal amount;
        try {
            amount = MoneyUtil.parsePositive(args[1], plugin.getEconomyService().scale());
        } catch (Exception e) {
            Chat.send(sender, plugin.getSettings(), "<red>Invalid amount.</red>");
            return true;
        }

        plugin.getEconomyService().transfer(player.getUniqueId(), player.getName(), target.getUniqueId(), target.getName(), amount)
                .whenComplete((ok, err) -> Bukkit.getScheduler().runTask(plugin, () -> {
                    if (err != null) {
                        Chat.send(player, plugin.getSettings(), "<red>Transfer failed.</red>");
                        return;
                    }
                    if (!ok) {
                        Chat.send(player, plugin.getSettings(), "<red>Insufficient funds.</red>");
                        return;
                    }
                    Chat.send(player, plugin.getSettings(), "<green>You sent $<amount> to <player>.</green>",
                            Map.of("amount", MoneyUtil.format(amount, plugin.getEconomyService().scale()), "player", target.getName()));
                    Chat.send(target, plugin.getSettings(), "<green>You received $<amount> from <player>.</green>",
                            Map.of("amount", MoneyUtil.format(amount, plugin.getEconomyService().scale()), "player", player.getName()));
                }));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
