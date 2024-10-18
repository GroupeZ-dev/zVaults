package fr.traqueur.vaults.gui;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.vaults.Vault;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VaultMenu implements InventoryHolder {

    private VaultsPlugin plugin;
    private final Vault vault;

    public VaultMenu(VaultsPlugin plugin, Vault vault) {
        this.plugin = plugin;
        this.vault = vault;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        String title = Configuration.getConfiguration(VaultsConfiguration.class).getVaultTitle();
        title = plugin.getMessageResolver().convertToLegacyFormat(title);
        var inv = Bukkit.createInventory(this, vault.getSize(), ChatColor.translateAlternateColorCodes('&', title));
        inv.setContents(vault.getContent().stream().map(vaultItem -> vaultItem.toItem(vault.isInfinite())).toArray(ItemStack[]::new));
        return inv;
    }

    public Vault getVault() {
        return vault;
    }
}
