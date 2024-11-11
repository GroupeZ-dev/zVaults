package fr.traqueur.vaults.commands;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.commands.VaultCommand;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.vaults.SizeMode;
import fr.traqueur.vaults.commands.admin.*;
import org.bukkit.command.CommandSender;

public class AdminCommand extends VaultCommand {
    public AdminCommand(VaultsPlugin plugin) {
        super(plugin, "admin");

        this.setPermission("zvaults.admin");

        if (Configuration.getConfiguration(VaultsConfiguration.class).getSizeMode() != SizeMode.DEFAULT) {
            this.addSubCommand(new GrowSizeCommand(plugin), new SetSizeCommand(plugin));
        }

        this.addSubCommand(new CreateCommand(plugin), new OpenCommand(plugin), new SetStackLimitCommand(plugin), new ConvertCommand(plugin));
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        this.getSubcommands()
                .stream().filter(sc -> sc instanceof VaultCommand)
                .map(sc -> (VaultCommand) sc)
                .forEach(subCommand -> {
                    if (commandSender.hasPermission(subCommand.getPermission())) {
                        commandSender.sendMessage("§e" + subCommand.getUsage() + " §7- §f" + subCommand.getDescription());
                    }
        });
    }
}
