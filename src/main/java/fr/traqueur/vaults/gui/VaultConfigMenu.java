package fr.traqueur.vaults.gui;

import fr.maxlego08.menu.ZInventory;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class VaultConfigMenu extends ZInventory {

    private final UserManager userManager;
    private final VaultConfigurationManager configurationManager;

    public VaultConfigMenu(Plugin plugin, String name, String fileName, int size, List<Button> buttons) {
        super(plugin, name, fileName, size, buttons);
        this.configurationManager = ((VaultsPlugin) plugin).getManager(VaultConfigurationManager.class);
        this.userManager = ((VaultsPlugin) plugin).getManager(UserManager.class);
    }

    @Override
    public void closeInventory(Player player, InventoryDefault inventoryDefault) {
        User user = this.userManager.getUser(player.getUniqueId()).orElseThrow();
        this.configurationManager.closeVaultConfig(user);
        if(Configuration.get(VaultsConfiguration.class).isCloseVaultOpenChooseMenu()) {
            ((VaultsPlugin) getPlugin()).getScheduler().runNextTick((t) -> {
                ((VaultsPlugin) getPlugin()).getManager(VaultsManager.class).openVaultChooseMenu(user, user);
            });
        }
    }
}
