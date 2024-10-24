package fr.traqueur.vaults.gui.buttons;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.events.VaultUpdateEvent;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VaultItemButton extends ZButton {


    private final VaultsPlugin plugin;
    private final VaultsConfiguration configuration;
    private final VaultsManager vaultsManager;
    private final UserManager userManager;
    private Vault vault;

    public VaultItemButton(Plugin plugin) {
        this.plugin = (VaultsPlugin) plugin;
        this.vaultsManager = this.plugin.getManager(VaultsManager.class);
        this.userManager =  this.plugin.getManager(UserManager.class);
        this.configuration =  Configuration.getConfiguration(VaultsConfiguration.class);
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onInventoryOpen(Player player, InventoryDefault inventory, Placeholders placeholders) {

        inventory.setDisablePlayerInventoryClick(false);
        User user = this.userManager.getUser(player.getUniqueId()).orElseThrow();
        this.vault = this.vaultsManager.getOpenedVault(user);
        if (this.vault == null) {
            player.closeInventory();
        }

        placeholders.register("vault_name", configuration.getVaultTitle(this.vault.getSize() + ""));
    }

    @Override
    public void onInventoryClose(Player player, InventoryDefault inventory) {
        User user = this.userManager.getUser(player.getUniqueId()).orElseThrow();
        this.vaultsManager.closeVault(user, vault);
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {
        int vaultSize = this.vault.getSize();
        for (int slot : this.slots) {
            ItemStack item;
            if (slot < vaultSize) {
                if (this.vault.getContent().size() <= slot || this.vault.getContent().isEmpty()) {
                    item = new VaultItem(new ItemStack(Material.AIR), 1, slot).toItem(player, this.vault.isInfinite());
                } else {
                    var vaultItem = this.vault.getContent().get(slot);
                    if(vaultItem.item() == null || vaultItem.item().getType().isAir()) {
                        continue;
                    }
                    item = vaultItem.toItem(player, this.vault.isInfinite());
                }
            } else {
                item = configuration.getIcon("empty_item").build(player);
            }
            inventory.addItem(slot, item).setClick(event -> event.setCancelled(true));
        }
    }

    @Override
    public void onDrag(InventoryDragEvent event, Player player, InventoryDefault inventoryDefault) {
        if(this.vault.isInfinite()) {
            event.setCancelled(true);
        }
        event.getNewItems().forEach((slot, item) -> {
            if(slot < this.vault.getSize()) {
                VaultItem vaultItem = vault.getInSlot(slot);
                VaultUpdateEvent vaultUpdateEvent = new VaultUpdateEvent(plugin, this.userManager.getUser(player.getUniqueId()).orElseThrow(), this.vault, vaultItem, slot);
                plugin.getServer().getPluginManager().callEvent(vaultUpdateEvent);
            }
        });
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event, Player player, InventoryDefault inventoryDefault) {

        ClickType clickType = event.getClick();
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        int slot = event.getRawSlot();
        int inventorySize = inventoryDefault.getSpigotInventory().getSize();

        if(slot >= inventorySize && slot >= vault.getSize() && !clickType.isShiftClick() && clickType != ClickType.DOUBLE_CLICK || slot < 0) {
            return;
        }

        event.setCancelled(true);

        switch (clickType) {
            case LEFT -> this.vaultsManager.handleLeftClick(event, player, cursor, slot, this.vault);
            case RIGHT -> this.vaultsManager.handleRightClick(event, player, cursor, current, slot, inventorySize, this.vault);
            case SHIFT_LEFT, SHIFT_RIGHT -> this.vaultsManager.handleShift(event, player, cursor, current, slot, inventorySize, this.vault);
            case DROP, CONTROL_DROP -> this.vaultsManager.handleDrop(event, player, cursor, current, slot, inventorySize, this.vault, clickType == ClickType.CONTROL_DROP);
            case NUMBER_KEY -> this.vaultsManager.handleNumberKey(event, player, cursor, current, slot, inventorySize, this.vault);
        }

    }
}
