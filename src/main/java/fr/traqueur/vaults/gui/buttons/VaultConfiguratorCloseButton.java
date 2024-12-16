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

public class VaultConfiguratorCloseButton extends ZButton {

    private final VaultsManager vaultsManager;
    private final VaultConfigurationManager vaultConfigurationManager;
    private final UserManager userManager;

    public VaultConfiguratorCloseButton(Plugin plugin) {
        this.vaultsManager = ((VaultsPlugin) plugin).getManager(VaultsManager.class);
        this.userManager =  ((VaultsPlugin) plugin).getManager(UserManager.class);
        this.vaultConfigurationManager = ((VaultsPlugin) plugin).getManager(VaultConfigurationManager.class);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        player.closeInventory();
    }
}
