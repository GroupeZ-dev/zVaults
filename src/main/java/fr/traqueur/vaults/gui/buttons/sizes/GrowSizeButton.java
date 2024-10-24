package fr.traqueur.vaults.gui.buttons.sizes;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;

public class GrowSizeButton extends ManipulationSizeButton {

    public GrowSizeButton(VaultsPlugin plugin, int size) {
        super(plugin, size);
    }

    @Override
    public void execute(User user, InventoryDefault inventory, int slot, Placeholders placeholders, Vault vault) {
        this.vaultsManager.changeSizeOfVault(user, vault, vault.getSize() + this.size, Message.VAULT_GROWED_SUCCESS, Message.VAULT_GROWED);
    }
}
