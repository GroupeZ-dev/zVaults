package fr.traqueur.vaults.gui.buttons.sizes;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class ManipulationSizeButton extends ZButton {

    protected final UserManager userManager;
    protected final VaultConfigurationManager vaultConfigurationManager;
    protected final VaultsManager vaultsManager;
    protected final int size;

    protected ManipulationSizeButton(VaultsPlugin plugin, int size) {
        this.size = size;
        this.userManager = plugin.getManager(UserManager.class);
        this.vaultConfigurationManager = plugin.getManager(VaultConfigurationManager.class);
        this.vaultsManager = plugin.getManager(VaultsManager.class);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            this.execute(user, inventory, slot, placeholders, this.vaultConfigurationManager.getOpenedConfig(user));
        });

    }

    public abstract void execute(User user, InventoryDefault inventory, int slot, Placeholders placeholders, Vault vault);
}
