package fr.traqueur.vaults.gui.buttons;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
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
    private Vault vault;

    public CustomizeIconButton(Plugin plugin) {
        this.plugin = (VaultsPlugin) plugin;
        this.userManager =  this.plugin.getManager(UserManager.class);
        this.vaultConfigurationManager = this.plugin.getManager(VaultConfigurationManager.class);
    }

    @Override
    public void onInventoryOpen(Player player, InventoryDefault inventory, Placeholders placeholders) {
        User user = this.userManager.getUser(player.getUniqueId()).orElseThrow();
        vault = this.vaultConfigurationManager.getOpenedConfig(user);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        User user = this.userManager.getUser(player.getUniqueId()).orElseThrow();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType().isAir()) {
            user.sendMessage(Message.ITEM_CANT_BE_NULL);
            return;
        }
        vault.setIcon(itemInHand.getType());
        user.sendMessage(Message.VAULT_ICON_CHANGE, Formatter.format("%new_icon%", MaterialLocalization.getTranslateName(itemInHand.getType())));
    }
}
