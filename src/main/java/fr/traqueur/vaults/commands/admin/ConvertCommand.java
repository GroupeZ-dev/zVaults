package fr.traqueur.vaults.commands.admin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.commands.VaultCommand;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.converters.Converters;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class ConvertCommand extends VaultCommand {

    public ConvertCommand(VaultsPlugin plugin) {
        super(plugin, "convert");

        this.addArgs("plugin", Converters.class, ((commandSender, list) -> Arrays.stream(Converters.values()).filter(Converters::isEnable).map(Converters::getName).toList()));

        this.setPermission("zvaults.admin.convert");
        this.setUsage("/vaults convert <plugin>");
        this.setDescription(plugin.getMessageResolver().convertToLegacySectionFormat(Message.CONVERT_COMMAND_DESCRIPTION.translate()));
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {

        Converters converter = arguments.get("plugin");
        if (!converter.isEnable()) {
            getPlugin().getMessageResolver().sendMessage(commandSender, Message.PLUGIN_TO_CONVERT_NOT_ENABLE.translate());
            return;
        }

        converter.convert();
        getPlugin().getMessageResolver().sendMessage(commandSender, Message.CONVERT_COMMAND_SUCCESS.translate(Formatter.format("%plugin%", converter.getName())));
    }
}
