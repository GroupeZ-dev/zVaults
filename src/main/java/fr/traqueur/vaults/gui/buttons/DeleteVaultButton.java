package fr.traqueur.vaults.gui.buttons;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

public class DeleteVaultButton extends ZButton {

    private final UserManager userManager;
    private final VaultConfigurationManager vaultConfigurationManager;
    private final VaultsManager vaultsManager;

    public DeleteVaultButton(Plugin plugin) {
        VaultsPlugin vaultsPlugin = (VaultsPlugin) plugin;
        this.userManager = vaultsPlugin.getManager(UserManager.class);
        this.vaultConfigurationManager = vaultsPlugin.getManager(VaultConfigurationManager.class);
        this.vaultsManager = vaultsPlugin.getManager(VaultsManager.class);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Vault vault = this.vaultConfigurationManager.getOpenedConfig(user);
            this.vaultsManager.deleteVault(vault, true);
        });
        player.closeInventory();
    }
}
