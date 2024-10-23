package fr.traqueur.vaults.api.vaults;

import fr.maxlego08.menu.MenuItemStack;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.serialization.Base64;
import fr.traqueur.vaults.api.serialization.MaterialLocalization;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;

public record VaultItem(ItemStack item, int amount, int slot) {

    public static VaultItem deserialize(String serialized) {
        String[] parts = serialized.split(":");
        return new VaultItem(Base64.decodeItem(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    public boolean isEmpty() {
        return this.item == null || this.item.getType().isAir();
    }

    public String serialize() {
        return Base64.encodeItem(this.item) + ":" + this.amount + ":" + this.slot;
    }

    public ItemStack toItem(Player player, boolean infinite) {
        if(item == null) {
            return new ItemStack(Material.AIR);
        }

        if(infinite) {
            if(item.getType().isAir()) {
                return item;
            }

            MenuItemStack item = Configuration.getConfiguration(VaultsConfiguration.class).getIcon("vault_item");

            Placeholders placeholders = new Placeholders();
            placeholders.register("material_name", MaterialLocalization.getTranslateName(this.item.getType()));
            placeholders.register("amount", this.formatNumber(this.amount));
            placeholders.register("material", this.item.getType().name());

            ItemStack templateItem = item.build(player, true, placeholders);
            ItemStack menuItem = this.item.clone();
            ItemMeta meta = menuItem.getItemMeta();
            ItemMeta templateMeta = templateItem.getItemMeta();

            meta.setDisplayName(templateMeta.getDisplayName());
            meta.setLore(templateMeta.getLore());
            menuItem.setItemMeta(meta);


            return menuItem;
        } else {
            this.item.setAmount(this.amount);
        }

        return this.item;
    }

    private String formatNumber(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (value >= 1_000_000_000) {
            return decimalFormat.format(value / 1_000_000_000) + "B";
        } else if (value >= 1_000_000) {
            return decimalFormat.format(value / 1_000_000) + "M";
        } else if (value >= 1_000) {
            return decimalFormat.format(value / 1_000) + "k";
        } else {
            return String.valueOf((int) value); // For numbers less than 1000
        }
    }

}
