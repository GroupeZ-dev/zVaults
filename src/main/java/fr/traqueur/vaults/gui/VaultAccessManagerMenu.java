package fr.traqueur.vaults.gui;

import fr.maxlego08.menu.ZInventory;
import fr.maxlego08.menu.api.button.Button;
import fr.traqueur.vaults.gui.buttons.UserAccessButton;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;

public class VaultAccessManagerMenu extends ZInventory {

    public VaultAccessManagerMenu(Plugin plugin, String name, String fileName, int size, List<Button> buttons) {
        super(plugin, name, fileName, size, buttons);
    }

    @Override
    public int getMaxPage(Player player, Object... objects) {
        int maxPage = super.getMaxPage(player, objects);
        int currentMaxPage = 1;

        Optional<UserAccessButton> optional = this.getButtons(UserAccessButton.class).stream().findFirst();
        if (optional.isPresent()) {
            UserAccessButton button = optional.get();

            int elementSize = button.getPaginationSize(player);

            if (elementSize >= 1) {
                int size = button.getSlots().size();
                int toReturn = ((elementSize / (size)) + (elementSize == (size) ? 0 : 1));
                return toReturn == 0 ? 1 : toReturn;
            }
        }

        return Math.max(maxPage, currentMaxPage);
    }
}
