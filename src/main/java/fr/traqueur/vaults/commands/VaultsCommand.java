package fr.traqueur.vaults.commands;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import fr.traqueur.vaults.api.VaultsPlugin;
import org.bukkit.command.CommandSender;

public class VaultsCommand extends Command<VaultsPlugin> {

    public VaultsCommand(VaultsPlugin plugin) {
        super(plugin, "zvaults");
        this.addSubCommand(new CreateCommand(plugin));
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

    }
}
