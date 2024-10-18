package fr.traqueur.vaults.gui;

import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.vaults.Vault;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VaultMenu implements InventoryHolder {

    private final Vault vault;

    public VaultMenu(Vault vault) {
        this.vault = vault;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        String title = Configuration.getConfiguration(VaultsConfiguration.class).getVaultTitle();
        var inv = Bukkit.createInventory(this, vault.getSize(), title);
        inv.setContents(vault.getContent().stream().map(vaultItem -> vaultItem.toItem(vault.isInfinite())).toArray(ItemStack[]::new));
        return inv;
    }

    public Vault getVault() {
        return vault;
    }
}
