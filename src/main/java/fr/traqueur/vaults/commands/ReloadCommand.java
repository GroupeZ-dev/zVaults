package fr.traqueur.vaults.commands;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.commands.VaultCommand;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.messages.Message;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends VaultCommand {

    public ReloadCommand(VaultsPlugin plugin) {
        super(plugin, "reload");

        this.setUsage("/zvaults reload");
        this.setDescription("Reload le plugin.");
        this.setPermission("zvaults.admin.reload");
    }


    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        Configuration.REGISTRY.values().forEach(Configuration::load);
        this.getPlugin().loadCommands();
        this.getPlugin().loadInventories();
        this.getPlugin().getMessageResolver().sendMessage(commandSender, Message.RELOAD_PLUGIN_SUCCESS.translate());
    }
}
