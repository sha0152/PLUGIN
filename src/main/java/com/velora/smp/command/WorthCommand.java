package com.velora.smp.command;

import com.velora.smp.VeloraCore;
import com.velora.smp.util.Chat;
import com.velora.smp.util.MoneyUtil;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class WorthCommand implements TabExecutor {
    private final VeloraCore plugin;

    public WorthCommand(VeloraCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getSettings().moduleEnabled("selling")) {
            Chat.send(sender, plugin.getSettings(), "<red>Selling is disabled.</red>");
            return true;
        }

        Material material;
        int amount = 1;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                Chat.send(sender, plugin.getSettings(), "<red>Usage: /worth <item> [amount]</red>");
                return true;
            }
            ItemStack hand = player.getInventory().getItemInMainHand();
            material = hand.getType();
            amount = Math.max(1, hand.getAmount());
        } else {
            material = Material.matchMaterial(args[0]);
            if (material == null) {
                Chat.send(sender, plugin.getSettings(), "<red>Unknown item.</red>");
                return true;
            }
            if (args.length > 1) {
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    Chat.send(sender, plugin.getSettings(), "<red>Invalid amount.</red>");
                    return true;
                }
            }
        }

        var unit = plugin.getPriceService().priceFor(material);
        if (unit.isEmpty()) {
            Chat.send(sender, plugin.getSettings(), "<red>Item is not sellable.</red>");
            return true;
        }

        BigDecimal total = unit.get().multiply(BigDecimal.valueOf(amount));
        Chat.send(sender, plugin.getSettings(), "<gray>Worth:</gray> <green>$<amount></green>",
                Map.of("amount", MoneyUtil.format(total, plugin.getEconomyService().scale())));
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
