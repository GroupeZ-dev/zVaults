package fr.traqueur.vaults.gui;

import fr.maxlego08.menu.ZInventory;
import fr.maxlego08.menu.api.button.Button;
import fr.traqueur.vaults.gui.buttons.VaultButton;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;

public class VaultsChooseMenu extends ZInventory {

    public VaultsChooseMenu(Plugin plugin, String name, String fileName, int size, List<Button> buttons) {
        super(plugin, name, fileName, size, buttons);
    }

    @Override
    public int getMaxPage(Player player, Object... objects) {
        int maxPage = super.getMaxPage(player, objects);
        int currentMaxPage = 1;

        Optional<VaultButton> optional = this.getButtons(VaultButton.class).stream().findFirst();
        if (optional.isPresent()) {
            VaultButton button = optional.get();

            int elementSize = button.getPaginationSize(player);

            if (elementSize >= 1) {
                int size = button.getSlots().size();
                return ((elementSize / (size)) + (elementSize == (size) ? 0 : 1));
            }
        }

        return Math.max(maxPage, currentMaxPage);
    }

}
