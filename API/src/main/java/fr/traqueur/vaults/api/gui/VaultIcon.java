package fr.traqueur.vaults.api.gui;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Loadable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public record VaultIcon(Material material, String id, int customModelData, String name, List<String> lore)
        implements Loadable {

    public ItemStack toItem(VaultsPlugin plugin) {
        ItemStack item = new ItemStack(this.material);
        item.setAmount(1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getMessageResolver().convertToLegacyFormat(this.name)));
        meta.setLore(this.lore.stream().
                map(line -> ChatColor.translateAlternateColorCodes('&', plugin.getMessageResolver().convertToLegacyFormat(line))
                ).toList());
        meta.setCustomModelData(this.customModelData);
        item.setItemMeta(meta);
        return item;
    }
}
