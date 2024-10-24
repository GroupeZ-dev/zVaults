package fr.traqueur.vaults.gui.buttons;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

public class VaultInviteButton extends ZButton {

    private final UserManager userManager;
    private final VaultConfigurationManager vaultConfigurationManager;

    public VaultInviteButton(Plugin plugin) {
        this.userManager =  ((VaultsPlugin) plugin).getManager(UserManager.class);
        this.vaultConfigurationManager = ((VaultsPlugin) plugin).getManager(VaultConfigurationManager.class);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Vault vault = this.vaultConfigurationManager.getOpenedConfig(user);
            ClickType clickType = event.getClick();
            if (clickType == ClickType.LEFT) {
                this.vaultConfigurationManager.openInvitationMenu(user, vault);
            } else if (clickType == ClickType.RIGHT) {
                this.vaultConfigurationManager.openAccessManagerMenu(user, vault);
            }
        });
    }
}
