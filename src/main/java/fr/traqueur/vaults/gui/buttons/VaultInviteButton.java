package fr.traqueur.vaults.gui.buttons;

import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.button.ZButton;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class VaultInviteButton extends ZButton {

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        ClickType clickType = event.getClick();
        if (clickType == ClickType.LEFT) {
            //invite someone
        } else if (clickType == ClickType.RIGHT) {
            //manage invites
        }
    }
}
