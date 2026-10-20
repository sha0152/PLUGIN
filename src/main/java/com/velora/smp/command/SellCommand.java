package com.velora.smp.command;

import com.velora.smp.VeloraCore;
import com.velora.smp.util.Chat;
import com.velora.smp.util.MoneyUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class SellCommand implements TabExecutor {
    private final VeloraCore plugin;

    public SellCommand(VeloraCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getSettings().moduleEnabled("selling")) {
            Chat.send(sender, plugin.getSettings(), "<red>Selling is disabled.</red>");
            return true;
        }
        if (!(sender instanceof Player player)) {
            Chat.send(sender, plugin.getSettings(), "<red>Only players can sell.</red>");
            return true;
        }

        String mode = args.length == 0 ? "hand" : args[0].toLowerCase();
        switch (mode) {
            case "hand" -> sellHand(player);
            case "inventory", "all" -> sellInventory(player);
            default -> Chat.send(sender, plugin.getSettings(), "<yellow>Usage: /sell <hand|inventory|all></yellow>");
        }
        return true;
    }

    private void sellHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            Chat.send(player, plugin.getSettings(), "<red>Nothing in hand.</red>");
            return;
        }
        performSale(player, List.of(new SlotChange(-1, item.clone())));
    }

    private void sellInventory(Player player) {
        List<SlotChange> items = new ArrayList<>();
        ItemStack[] storage = player.getInventory().getStorageContents();
        for (int i = 0; i < storage.length; i++) {
            ItemStack item = storage[i];
            if (item != null && !item.getType().isAir()) {
                items.add(new SlotChange(i, item.clone()));
            }
        }
        if (items.isEmpty()) {
            Chat.send(player, plugin.getSettings(), "<red>No sellable items found.</red>");
            return;
        }
        performSale(player, items);
    }

    private void performSale(Player player, List<SlotChange> changes) {
        BigDecimal total = BigDecimal.ZERO.setScale(plugin.getEconomyService().scale());
        int itemCount = 0;

        for (SlotChange change : changes) {
            Material type = change.item().getType();
            var unit = plugin.getPriceService().priceFor(type);
            if (unit.isEmpty()) {
                continue;
            }
            int amount = change.item().getAmount();
            total = total.add(unit.get().multiply(BigDecimal.valueOf(amount)));
            itemCount += amount;
        }

        if (itemCount == 0 || total.compareTo(BigDecimal.ZERO) <= 0) {
            Chat.send(player, plugin.getSettings(), "<red>No sellable items found.</red>");
            return;
        }

        if (total.compareTo(plugin.getSettings().minSale()) < 0 || total.compareTo(plugin.getSettings().maxSale()) > 0) {
            Chat.send(player, plugin.getSettings(), "<red>Sale amount is outside allowed limits.</red>");
            return;
        }

        Map<Integer, ItemStack> restore = new HashMap<>();
        for (SlotChange change : changes) {
            if (plugin.getPriceService().priceFor(change.item().getType()).isEmpty()) {
                continue;
            }
            if (change.slot() == -1) {
                restore.put(-1, player.getInventory().getItemInMainHand().clone());
                player.getInventory().setItemInMainHand(null);
            } else {
                ItemStack existing = player.getInventory().getStorageContents()[change.slot()];
                if (existing != null) {
                    restore.put(change.slot(), existing.clone());
                    player.getInventory().setItem(change.slot(), null);
                }
            }
        }

        final int soldCount = itemCount;
        final BigDecimal soldTotal = total;
        plugin.getEconomyService().addBalance(player.getUniqueId(), player.getName(), soldTotal, null, "sell")
                .whenComplete((newBal, err) -> Bukkit.getScheduler().runTask(plugin, () -> {
                    if (err != null) {
                        for (Map.Entry<Integer, ItemStack> entry : restore.entrySet()) {
                            if (entry.getKey() == -1) {
                                player.getInventory().setItemInMainHand(entry.getValue());
                            } else {
                                player.getInventory().setItem(entry.getKey(), entry.getValue());
                            }
                        }
                        Chat.send(player, plugin.getSettings(), "<red>Sale failed and was rolled back.</red>");
                        return;
                    }
                    Chat.send(player, plugin.getSettings(), "<green>Sold <items> items for $<amount>.</green>",
                            Map.of("items", String.valueOf(soldCount), "amount", MoneyUtil.format(soldTotal, plugin.getEconomyService().scale())));
                }));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("hand", "inventory", "all").stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }

    private record SlotChange(int slot, ItemStack item) {}
}
