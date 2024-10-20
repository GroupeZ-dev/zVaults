package fr.traqueur.vaults.commands;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.vaults.SizeMode;
import fr.traqueur.vaults.commands.admin.CreateCommand;
import fr.traqueur.vaults.commands.admin.GrowSizeCommand;
import fr.traqueur.vaults.commands.admin.OpenCommand;
import fr.traqueur.vaults.commands.admin.SetSizeCommand;
import org.bukkit.command.CommandSender;

public class AdminCommand extends Command<VaultsPlugin> {
    public AdminCommand(VaultsPlugin plugin) {
        super(plugin, "admin");

        this.setPermission("vaults.admin");

        if (Configuration.getConfiguration(VaultsConfiguration.class).getSizeMode() != SizeMode.DEFAULT) {
            this.addSubCommand(new GrowSizeCommand(plugin), new SetSizeCommand(plugin));
        }

        this.addSubCommand(new CreateCommand(plugin), new OpenCommand(plugin));

        this.setGameOnly(true);
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {}
}
