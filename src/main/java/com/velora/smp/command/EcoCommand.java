package com.velora.smp.command;

import com.velora.smp.VeloraCore;
import com.velora.smp.util.Chat;
import com.velora.smp.util.MoneyUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public final class EcoCommand implements TabExecutor {
    private final VeloraCore plugin;

    public EcoCommand(VeloraCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getSettings().moduleEnabled("economy")) {
            Chat.send(sender, plugin.getSettings(), "<red>Economy is disabled.</red>");
            return true;
        }
        if (args.length < 2) {
            Chat.send(sender, plugin.getSettings(), "<yellow>Usage: /eco <give|take|set|reset|info> <player> [amount]</yellow>");
            return true;
        }

        String sub = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID targetId = target.getUniqueId();
        if (targetId == null) {
            Chat.send(sender, plugin.getSettings(), "<red>Player not found.</red>");
            return true;
        }

        if (sub.equals("info")) {
            plugin.getEconomyService().getBalance(targetId, target.getName() == null ? args[1] : target.getName())
                    .whenComplete((bal, err) -> Bukkit.getScheduler().runTask(plugin, () -> {
                        if (err != null) {
                            Chat.send(sender, plugin.getSettings(), "<red>Failed to load account.</red>");
                            return;
                        }
                        Chat.send(sender, plugin.getSettings(), "<gray><player>'s balance:</gray> <green>$<amount></green>",
                                Map.of("player", args[1], "amount", MoneyUtil.format(bal, plugin.getEconomyService().scale())));
                    }));
            return true;
        }

        if (args.length < 3) {
            Chat.send(sender, plugin.getSettings(), "<red>Amount required.</red>");
            return true;
        }

        BigDecimal amount;
        try {
            amount = MoneyUtil.parseNonNegative(args[2], plugin.getEconomyService().scale());
        } catch (Exception ex) {
            Chat.send(sender, plugin.getSettings(), "<red>Invalid amount.</red>");
            return true;
        }

        UUID actor = sender instanceof org.bukkit.entity.Player p ? p.getUniqueId() : null;
        String targetName = target.getName() == null ? args[1] : target.getName();
        switch (sub) {
            case "give" -> plugin.getEconomyService().addBalance(targetId, targetName, amount, actor, "eco give")
                    .whenComplete((bal, err) -> respond(sender, targetName, bal, err));
            case "take" -> plugin.getEconomyService().takeBalance(targetId, targetName, amount, actor, "eco take")
                    .whenComplete((bal, err) -> respond(sender, targetName, bal, err));
            case "set" -> plugin.getEconomyService().setBalance(targetId, targetName, amount, actor, "eco set")
                    .whenComplete((bal, err) -> respond(sender, targetName, bal, err));
            case "reset" -> plugin.getEconomyService().setBalance(targetId, targetName, BigDecimal.ZERO, actor, "eco reset")
                    .whenComplete((bal, err) -> respond(sender, targetName, bal, err));
            default -> Chat.send(sender, plugin.getSettings(), "<red>Unknown subcommand.</red>");
        }
        return true;
    }

    private void respond(CommandSender sender, String playerName, BigDecimal balance, Throwable err) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (err != null) {
                Chat.send(sender, plugin.getSettings(), "<red>Economy update failed.</red>");
                return;
            }
            Chat.send(sender, plugin.getSettings(), "<green>Economy updated for <player>: $<amount></green>",
                    Map.of("player", playerName, "amount", MoneyUtil.format(balance, plugin.getEconomyService().scale())));
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("give", "take", "set", "reset", "info").stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(org.bukkit.entity.Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        }
        return List.of();
    }
}
