package fr.traqueur.vaults.commands;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.vaults.SizeMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VaultsCommand extends Command<VaultsPlugin> {

    public VaultsCommand(VaultsPlugin plugin) {
        super(plugin, "zvaults");
        this.addSubCommand(new CreateCommand(plugin));

        if (Configuration.getConfiguration(VaultsConfiguration.class).getSizeMode() != SizeMode.DEFAULT) {
            this.addSubCommand(new GrowSizeCommand(plugin), new SetSizeCommand(plugin));
        }

        this.setGameOnly(true);
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        this.getPlugin().getInventoryManager().openInventory((Player) commandSender, "vaults_menu");
    }
}
