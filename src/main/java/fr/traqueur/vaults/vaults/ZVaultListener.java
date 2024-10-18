package fr.traqueur.vaults.vaults;

import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.gui.VaultMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class ZVaultListener implements Listener {

    private final VaultsManager vaultsManager;

    public ZVaultListener(VaultsManager vaultsManager) {
        this.vaultsManager = vaultsManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if(!(event.getInventory().getHolder() instanceof VaultMenu menu)) {
            return;
        }
        Vault vault = menu.getVault();
        if(vault.isInfinite() && event.getRawSlot() <= vault.getSize()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryDragEvent event) {
        if(!(event.getInventory().getHolder() instanceof VaultMenu menu)) {
            return;
        }
        Vault vault = menu.getVault();
        if(vault.isInfinite() && event.getRawSlots().stream().anyMatch(slot -> slot <= vault.getSize())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if(!(event.getInventory().getHolder() instanceof VaultMenu menu)) {
            return;
        }
        Vault vault = menu.getVault();
        vaultsManager.closeVault(vault);
    }

}
