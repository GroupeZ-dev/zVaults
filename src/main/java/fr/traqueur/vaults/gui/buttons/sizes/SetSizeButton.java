package fr.traqueur.vaults.gui.buttons.sizes;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;

public class SetSizeButton extends ManipulationSizeButton {

    protected SetSizeButton(VaultsPlugin plugin, int size) {
        super(plugin, size);
    }

    @Override
    public void execute(User user, InventoryDefault inventory, int slot, Placeholders placeholders) {
        this.vaultsManager.changeSizeOfVault(user, this.vault, this.size, Message.VAULT_SET_SIZE_SUCCESS, Message.VAULT_SET_SIZE);
    }
}