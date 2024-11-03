package fr.traqueur.vaults.gui.buttons;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class AutoPickupVaultButton extends ZButton {

    private final UserManager userManager;
    private final VaultConfigurationManager vaultConfigurationManager;

    public AutoPickupVaultButton(Plugin plugin) {
        VaultsPlugin vaultsPlugin = (VaultsPlugin) plugin;
        this.userManager = vaultsPlugin.getManager(UserManager.class);
        this.vaultConfigurationManager = vaultsPlugin.getManager(VaultConfigurationManager.class);
    }

    @Override
    public ItemStack getCustomItemStack(Player player) {
        Placeholders placeholders = new Placeholders();
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Vault vault = this.vaultConfigurationManager.getOpenedConfig(user);
            placeholders.register("autopickup", Configuration.getConfiguration(VaultsConfiguration.class).getAutoPickupValue(vault.isAutoPickup()));
        });
        return super.getItemStack().build(player, true, placeholders);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Vault vault = this.vaultConfigurationManager.getOpenedConfig(user);
            vault.setAutoPickup(!vault.isAutoPickup());
        });
    }
}
