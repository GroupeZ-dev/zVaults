package fr.traqueur.vaults.commands;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.messages.Message;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends Command<VaultsPlugin> {

    public ReloadCommand(VaultsPlugin plugin) {
        super(plugin, "reload");

        this.setUsage("/zvaults reload");
        this.setDescription("Reload le plugin.");
        this.setPermission("vaults.command.reload");
    }


    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        this.getPlugin().loadInventories();
        Configuration.REGISTRY.values().forEach(Configuration::load);
        this.getPlugin().loadCommands();
        this.getPlugin().getMessageResolver().sendMessage(commandSender, Message.RELOAD_PLUGIN_SUCCESS.translate());
    }
}
