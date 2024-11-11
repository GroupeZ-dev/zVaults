package fr.traqueur.vaults.api.commands;

import fr.traqueur.commands.api.Command;
import fr.traqueur.vaults.api.VaultsPlugin;

public abstract class VaultCommand extends Command<VaultsPlugin> {

    public VaultCommand(VaultsPlugin plugin, String name) {
        super(plugin, name);
    }

}
