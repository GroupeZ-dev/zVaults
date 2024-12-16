package fr.traqueur.vaults.gui;

import fr.maxlego08.menu.ZInventory;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class VaultMenu extends ZInventory {

    private final VaultsManager vaultsManager;
    private final UserManager userManager;

    public VaultMenu(Plugin plugin, String name, String fileName, int size, List<Button> buttons) {
        super(plugin, name, fileName, size, buttons);
        this.vaultsManager = ((VaultsPlugin) plugin).getManager(VaultsManager.class);
        this.userManager = ((VaultsPlugin) plugin).getManager(UserManager.class);
    }

    @Override
    public void postOpenInventory(Player player, InventoryDefault inventoryDefault) {
        User user = this.userManager.getUser(player.getUniqueId()).orElseThrow();
        this.vaultsManager.linkVaultToInventory(user, inventoryDefault);
    }

    @Override
    public void closeInventory(Player player, InventoryDefault inventoryDefault) {
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Vault vault = this.vaultsManager.getOpenedVault(user);
            this.vaultsManager.closeVault(user, vault);
        });
    }
}
