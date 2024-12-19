package fr.traqueur.vaults.commands.admin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.users.ZUserManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BindCommand extends Command<VaultsPlugin> {

    private final UserManager userManager;
    private final VaultsManager vaultsManager;

    public BindCommand(VaultsPlugin plugin) {
        super(plugin, "bind");

        this.userManager = plugin.getManager(UserManager.class);
        this.vaultsManager = plugin.getManager(VaultsManager.class);

        this.setPermission("zvaults.admin.bind");
        this.setUsage("/vaults bind <receiver> <vault> <id>");
        this.setDescription(plugin.getMessageResolver().convertToLegacySectionFormat(Message.BIND_COMMAND_DESCRIPTION.translate()));

        this.addArgs("receiver:user");
        this.addArgs("vault:vault");
        this.addOptionalArgs("id", Long.class);
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        User user;
        if(!(commandSender instanceof Player)) {
            user = ZUserManager.CONSOLE_USER;
        } else {
            user = this.userManager.getUser(((Player) commandSender).getUniqueId()).orElseThrow();
        }

        User receiver = arguments.get("receiver");
        long id = arguments.get("id");
        Vault vault = arguments.get("vault");

        if(!vault.hasAccess(receiver)) {
            user.sendMessage(Message.USER_NOT_OWNER, Formatter.format("%user%", receiver.getName()));
            return;
        }

        if(this.vaultsManager.idExists(vault.getOwner(), id)) {
            user.sendMessage(Message.VAULT_ID_ALREADY_EXISTS, Formatter.format("%id%", id));
            return;
        }

        vault.setId(id);
        user.sendMessage(Message.VAULT_BIND, Formatter.format("%id%", id));
    }
}
