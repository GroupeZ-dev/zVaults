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
import org.bukkit.Bukkit;
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
import java.util.stream.Collectors;

public class VaultItemButton extends ZButton {


    private final VaultsPlugin plugin;
    private final VaultsConfiguration configuration;
    private final VaultsManager vaultsManager;
    private final UserManager userManager;

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
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Vault vault = this.vaultsManager.getOpenedVault(user);
            placeholders.register("vault_name", configuration.getVaultTitle(vault.getSize() + ""));
        });
    }

    @Override
    public void onInventoryClose(Player player, InventoryDefault inventory) {
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Vault vault = this.vaultsManager.getOpenedVault(user);
            this.vaultsManager.closeVault(user, vault);
            if(Configuration.getConfiguration(VaultsConfiguration.class).isCloseVaultOpenChooseMenu()) {
                this.vaultsManager.openVaultChooseMenu(user, user);
            }
        });
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Vault vault = this.vaultsManager.getOpenedVault(user);
            int vaultSize = vault.getSize();
            for (int slot : this.slots) {
                ItemStack item;
                if (slot < vaultSize) {
                    if (vault.getContent().size() <= slot || vault.getContent().isEmpty()) {
                        item = new VaultItem(new ItemStack(Material.AIR), 1, slot).toItem(player, vault.isInfinite());
                    } else {
                        var vaultItem = vault.getContent().get(slot);
                        if(vaultItem.item() == null || vaultItem.item().getType().isAir()) {
                            continue;
                        }
                        item = vaultItem.toItem(player, vault.isInfinite());
                    }
                } else {
                    item = configuration.getIcon("empty-item").build(player);
                }
                inventory.addItem(slot, item).setClick(event -> event.setCancelled(true));
            }
        });
    }

    @Override
    public void onDrag(InventoryDragEvent event, Player player, InventoryDefault inventoryDefault) {
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Vault vault = this.vaultsManager.getOpenedVault(user);
            if(vault.isInfinite()) {
                event.setCancelled(true);
                return;
            }
            event.getNewItems().forEach((slot, item) -> {
                if(slot < vault.getSize()) {
                    VaultItem vaultItem = vault.getInSlot(slot);
                    VaultItem newVaultItem = new VaultItem(this.vaultsManager.cloneItemStack(item), item.getAmount(), vaultItem.slot());
                    vault.setContent(vault.getContent().stream().map(itemInner -> itemInner.slot() == newVaultItem.slot() ? newVaultItem : itemInner).collect(Collectors.toList()));
                    VaultUpdateEvent vaultUpdateEvent = new VaultUpdateEvent(this.plugin, user, vault, newVaultItem, newVaultItem.slot());
                    Bukkit.getPluginManager().callEvent(vaultUpdateEvent);
                }
            });
        });
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event, Player player, InventoryDefault inventoryDefault) {
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Vault vault = this.vaultsManager.getOpenedVault(user);
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
                case LEFT -> this.vaultsManager.handleLeftClick(event, player, cursor, slot, vault);
                case RIGHT -> this.vaultsManager.handleRightClick(event, player, cursor, current, slot, inventorySize, vault);
                case SHIFT_LEFT, SHIFT_RIGHT -> this.vaultsManager.handleShift(event, player, cursor, current, slot, inventorySize, vault);
                case DROP, CONTROL_DROP -> this.vaultsManager.handleDrop(event, player, cursor, current, slot, inventorySize, vault, clickType == ClickType.CONTROL_DROP);
                case NUMBER_KEY -> this.vaultsManager.handleNumberKey(event, player, cursor, current, slot, inventorySize, vault);
            }
        });

    }
}
