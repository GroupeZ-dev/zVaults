package fr.traqueur.vaults.gui.buttons;

import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class VaultButton extends ZButton implements PaginateButton {

    private final VaultsManager vaultsManager;
    private final UserManager userManager;
    private final VaultConfigurationManager vaultConfigurationManager;

    public VaultButton(Plugin plugin) {
        this.vaultsManager = ((VaultsPlugin) plugin).getManager(VaultsManager.class);
        this.userManager =  ((VaultsPlugin) plugin).getManager(UserManager.class);
        this.vaultConfigurationManager = ((VaultsPlugin) plugin).getManager(VaultConfigurationManager.class);
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onInventoryClose(Player player, InventoryDefault inventory) {
        User user = this.userManager.getUser(player.getUniqueId()).orElseThrow();
        this.vaultsManager.closeVaultChooseMenu(user);
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {
        displayItems(player, inventory);
    }

    private void displayItems(Player player, InventoryDefault inventory) {
        VaultsConfiguration configuration = Configuration.get(VaultsConfiguration.class);
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Pagination<Vault> pagination = new Pagination<>();
            User target = this.vaultsManager.getTargetUser(user);
            if(target == null) {
                target = user;
            }
            List<Vault> vaults = this.vaultsManager.getVaults(target);
            List<Vault> buttons = pagination.paginate(vaults, this.slots.size(), inventory.getPage());

            for (int i = 0; i != Math.min(buttons.size(), this.slots.size()); i++) {
                int slot = slots.get(i);
                Vault vault = buttons.get(i);

                Placeholders placeholders = new Placeholders();
                placeholders.register("vault_name", vault.getName());
                placeholders.register("vault_icon", vault.getIcon().name());
                placeholders.register("vault_size", vault.getSize() +"");
                int contentSize = vault.getContent().stream().filter(vaultItem -> vaultItem.item() != null && !vaultItem.item().getType().isAir()).toList().size();
                placeholders.register("vault_content_size", contentSize + "");
                placeholders.register("vault_max_stack_size", vault.getMaxStackSize() + "");

                inventory.addItem(slot, configuration.getIcon("open-vault").build(player, false, placeholders)).setClick(event -> {
                    if(event.getClick() == ClickType.LEFT) {
                        event.getInventory().setItem(event.getRawSlot(), configuration.getIcon("loading-open").build(player, false, placeholders));
                        this.vaultsManager.openVault(user, vault);
                    } else if(event.getClick() == ClickType.RIGHT) {
                        if(!vault.isOwner(user) && !user.hasPermission("zvaults.admin.open")) {
                            user.sendMessage(Message.NOT_PERMISSION_CONFIGURE_VAULT);
                            return;
                        }
                        this.vaultConfigurationManager.openVaultConfig(user, vault);
                    }
                });
            }
        });
    }

    @Override
    public int getPaginationSize(Player player) {
        User user = this.userManager.getUser(player.getUniqueId()).orElseThrow();
        return this.vaultsManager.getVaults(user).size();
    }
}
