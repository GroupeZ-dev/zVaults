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
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class UserAccessButton extends ZButton implements PaginateButton {

    private final UserManager userManager;
    private final VaultConfigurationManager vaultConfigurationManager;

    public UserAccessButton(Plugin plugin) {
        this.userManager =  ((VaultsPlugin) plugin).getManager(UserManager.class);
        this.vaultConfigurationManager = ((VaultsPlugin) plugin).getManager(VaultConfigurationManager.class);
    }

    @Override
    public void onInventoryClose(Player player, InventoryDefault inventory) {
        this.userManager.getUser(player.getUniqueId())
                .ifPresent(this.vaultConfigurationManager::closeAccessManagerMenu);
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {
        this.userManager.getUser(player.getUniqueId()).ifPresent(user -> {
            Vault vault = this.vaultConfigurationManager.getOpenedAccessManager(user);
            List<User> users = this.vaultConfigurationManager.getWhoCanAccess(vault);
            Pagination<User> pagination = new Pagination<>();
            List<User> buttons = pagination.paginate(users, this.slots.size(), inventory.getPage());

            for (int i = 0; i != Math.min(buttons.size(), this.slots.size()); i++) {
                int slot = slots.get(i);
                User value = buttons.get(i);

                inventory.addItem(slot, this.getItem(user, value)).setClick(event -> {
                    if(event.getClick() == ClickType.LEFT) {
                        this.vaultConfigurationManager.removeAccess(user, vault, value);
                        event.getInventory().setItem(slot, new ItemStack(Material.AIR));
                    }
                });
            }
        });
    }

    private ItemStack getItem(User user, User value) {
        VaultsConfiguration configuration = Configuration.getConfiguration(VaultsConfiguration.class);
        var menuItem = configuration.getIcon("user_access_vault_item");
        Placeholders placeholders = new Placeholders();
        placeholders.register("player_name", value.getName());

        ItemStack item = menuItem.build(user.getPlayer(), true, placeholders);
        ItemMeta meta = item.getItemMeta();
        if(meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(value.getUniqueId()));
            item.setItemMeta(skullMeta);
        }
        return item;
    }

    @Override
    public int getPaginationSize(Player player) {
        User user = this.userManager.getUser(player.getUniqueId()).orElseThrow();
        Vault vault = this.vaultConfigurationManager.getOpenedAccessManager(user);
        return this.vaultConfigurationManager.getWhoCanAccess(vault).size();
    }
}
