package fr.traqueur.vaults.gui.buttons;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class VaultItemButton extends ZButton {


    private final VaultsPlugin plugin;
    private final VaultsManager vaultsManager;
    private final UserManager userManager;
    private Vault vault;

    public VaultItemButton(Plugin plugin) {
        this.plugin = (VaultsPlugin) plugin;
        this.vaultsManager = this.plugin.getManager(VaultsManager.class);
        this.userManager =  this.plugin.getManager(UserManager.class);
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onInventoryOpen(Player player, InventoryDefault inventory) {

        inventory.setDisablePlayerInventoryClick(false);
        User user = this.userManager.getUser(player.getUniqueId()).orElseThrow();
        this.vault = this.vaultsManager.getOpenedVault(user);
        if (this.vault == null) {
            player.closeInventory();
        }
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {
        VaultsConfiguration configuration = Configuration.getConfiguration(VaultsConfiguration.class);
        int vaultSize = this.vault.getSize();
        for (int slot : this.slots) {
            ItemStack item;
            Consumer<InventoryClickEvent> click;
            if (slot < vaultSize) {
                if (this.vault.getContent().size() <= slot || this.vault.getContent().isEmpty()) {
                    item = new VaultItem(new ItemStack(Material.AIR), 1).toItem(player, this.vault.isInfinite());
                } else {
                    item = this.vault.getContent().get(slot).toItem(player, this.vault.isInfinite());
                }
                click = event -> {
                    if(this.vault.isInfinite()) {
                        event.setCancelled(true);
                    }
                    player.sendMessage("You clicked on the item");
                };
            } else {
                item = configuration.getIcon("empty_item").build(player);
                click = event -> {
                    event.setCancelled(true);
                };
            }
            inventory.addItem(slot, item).setClick(click);
        }

    }
}
