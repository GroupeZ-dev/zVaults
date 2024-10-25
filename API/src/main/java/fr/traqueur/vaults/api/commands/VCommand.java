package fr.traqueur.vaults.api.commands;

import fr.traqueur.commands.api.Command;
import fr.traqueur.vaults.api.VaultsPlugin;

public abstract class VCommand extends Command<VaultsPlugin> {

    public VCommand(VaultsPlugin plugin, String name) {
        super(plugin, name);
    }

    public String usage() {
        return super.getUsage();
    }

    public String description() {
        return super.getDescription();
    }

}
