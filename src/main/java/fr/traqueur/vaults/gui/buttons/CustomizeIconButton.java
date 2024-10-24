package fr.traqueur.vaults.gui.buttons;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.events.VaultChangeIconEvent;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.serialization.MaterialLocalization;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class CustomizeIconButton extends ZButton {

    private final VaultsPlugin plugin;
    private final UserManager userManager;
    private final VaultConfigurationManager vaultConfigurationManager;

    public CustomizeIconButton(Plugin plugin) {
        this.plugin = (VaultsPlugin) plugin;
        this.userManager =  this.plugin.getManager(UserManager.class);
        this.vaultConfigurationManager = this.plugin.getManager(VaultConfigurationManager.class);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Vault vault = this.vaultConfigurationManager.getOpenedConfig(user);

            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand == null || itemInHand.getType().isAir()) {
                user.sendMessage(Message.ITEM_CANT_BE_NULL);
                return;
            }
            vault.setIcon(itemInHand.getType());
            user.sendMessage(Message.VAULT_ICON_CHANGE, Formatter.format("%new_icon%", MaterialLocalization.getTranslateName(itemInHand.getType())));
            VaultChangeIconEvent vaultChangeIconEvent = new VaultChangeIconEvent(this.plugin, vault, itemInHand.getType());
            this.plugin.getServer().getPluginManager().callEvent(vaultChangeIconEvent);

        });
    }
}
