package fr.traqueur.vaults.gui.buttons;

import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
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

public class VaultButton extends ZButton implements PaginateButton {

    private final VaultsPlugin plugin;
    private final VaultsManager vaultsManager;
    private final UserManager userManager;
    private Vault vault;

    public VaultButton(Plugin plugin) {
        this.plugin = (VaultsPlugin) plugin;
        this.vaultsManager = this.plugin.getManager(VaultsManager.class);
        this.userManager =  this.plugin.getManager(UserManager.class);;
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {
        displayItems(player, inventory);
    }

    private void displayItems(Player player, InventoryDefault inventory) {
        VaultsConfiguration configuration = Configuration.getConfiguration(VaultsConfiguration.class);
        Pagination<Vault> pagination = new Pagination<>();
        User user = this.userManager.getUser(player.getUniqueId()).orElseThrow();
        List<Vault> vaults = this.vaultsManager.getVaults(user);
        List<Vault> buttons = pagination.paginate(vaults, this.slots.size(), inventory.getPage());

        for (int i = 0; i != Math.min(buttons.size(), this.slots.size()); i++) {
            int slot = slots.get(i);
            Vault vault = buttons.get(i);

            inventory.addItem(slot, configuration.getIcon("open_vault").build(player)).setClick(event -> {
                this.vaultsManager.openVault(user, vault);
            });
        }
    }

    @Override
    public int getPaginationSize(Player player) {
        User user = this.userManager.getUser(player.getUniqueId()).orElseThrow();
        return this.vaultsManager.getVaults(user).size();
    }
}
