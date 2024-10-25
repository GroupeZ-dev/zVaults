package fr.traqueur.vaults.commands.admin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.commands.VCommand;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenCommand extends VCommand {

    private final UserManager userManager;
    private final VaultsManager vaultsManager;

    public OpenCommand(VaultsPlugin plugin) {
        super(plugin, "open");
        this.userManager = plugin.getManager(UserManager.class);
        this.vaultsManager = plugin.getManager(VaultsManager.class);

        this.setPermission("zvaults.admin.open");
        this.setUsage("/zvaults open <player>");
        this.setDescription(plugin.getMessageResolver().convertToLegacySectionFormat(Message.OPEN_COMMAND_DESCRIPTION.translate()));

        this.addArgs("target:user");

        this.setGameOnly(true);
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        User user = this.userManager.getUser(((Player) commandSender).getUniqueId()).orElseThrow();
        User target = arguments.get("target");
        this.vaultsManager.openVaultChooseMenu(user, target);
    }
}
