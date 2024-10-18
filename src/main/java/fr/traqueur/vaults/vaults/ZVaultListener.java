package fr.traqueur.vaults.vaults;

import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.gui.VaultMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ZVaultListener implements Listener {

    private final VaultsManager vaultsManager;

    public ZVaultListener(VaultsManager vaultsManager) {
        this.vaultsManager = vaultsManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        var inventory = event.getInventory();
        var player = (Player) event.getWhoClicked();
        if(!(inventory.getHolder() instanceof VaultMenu menu)) {
            return;
        }
        Vault vault = menu.getVault();
        ClickType click = event.getClick();
        ItemStack cursor = event.getCursor() == null ? new ItemStack(Material.AIR) : event.getCursor().clone();
        int firstEmpty = inventory.firstEmpty();
        int correspondingSlot = -1;
        if(vault.isInfinite() && event.getRawSlot() <= vault.getSize()) {
            event.setCancelled(true);
            if (firstEmpty == -1) {
                return;
            }
            if(!cursor.getType().isAir()) {
                correspondingSlot = inventory.first(cursor.getType());
            }
            if(correspondingSlot == -1) {
                return;
            }
        }

        int slot = correspondingSlot == -1 ? firstEmpty : correspondingSlot;

        switch (click) {
            case RIGHT -> {
                if(vault.isInfinite() && event.getRawSlot() <= vault.getSize()) {
                    addToItem(inventory, slot, 1, player);
                    if (cursor.getAmount() == 1) {
                        event.getView().setCursor(new ItemStack(Material.AIR));
                    } else {
                        cursor.setAmount(cursor.getAmount() - 1);
                        event.getView().setCursor(cursor);
                    }
                }
            }
            case LEFT -> {
                if(vault.isInfinite() && event.getRawSlot() <= vault.getSize()) {
                    addToItem(inventory, slot, cursor.getAmount(), player);
                    event.getView().setCursor(new ItemStack(Material.AIR));
                }
            }
            default -> {
                player.sendMessage("Not implemented yet");
            }
        }
    }

    private void addToItem(Inventory inventory, int slot, int amount, Player player) {
        ItemStack current = inventory.getItem(slot);
        if(current == null) {
            return;
        }
        ItemMeta meta = current.getItemMeta();
        if (meta == null) {
            return;
        }
        int oldAmount = meta.getPersistentDataContainer().getOrDefault(vaultsManager.getAmountKey(), PersistentDataType.INTEGER, 0);
        int newAmount = oldAmount + amount;
        VaultItem vaultItem = new VaultItem(current, newAmount);
        inventory.setItem(slot, vaultItem.toItem(player, true));
    }

    @EventHandler
    public void onClick(InventoryDragEvent event) {
        var inventory = event.getInventory();
        if(!(inventory.getHolder() instanceof VaultMenu menu)) {
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
