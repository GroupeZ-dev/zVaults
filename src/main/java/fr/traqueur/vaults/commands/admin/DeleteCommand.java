package fr.traqueur.vaults.commands.admin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.exceptions.IndexOutOfBoundVaultException;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.users.ZUserManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DeleteCommand extends Command<VaultsPlugin> {

    private final UserManager userManager;
    private final VaultsManager vaultsManager;

    public DeleteCommand(VaultsPlugin plugin) {
        super(plugin, "delete");

        this.userManager = plugin.getManager(UserManager.class);
        this.vaultsManager = plugin.getManager(VaultsManager.class);

        this.setPermission("vaults.command.delete");
        this.setUsage("/zvaults admin delete <player> <vault_number>");

        this.addArgs("receiver:user");
        this.addArgs("vault_num:int", (sender, args) -> this.vaultsManager.getNumVaultsTabulation());
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
        int vaultNum = arguments.get("vault_num");
        Vault vault;
        try {
            vault = this.vaultsManager.getVault(receiver, vaultNum);
        } catch (IndexOutOfBoundVaultException e) {
            user.sendMessage(Message.VAULT_NOT_FOUND);
            return;
        }
        this.vaultsManager.deleteVault(vault, true);
        user.sendMessage(Message.VAULT_DELETED);
    }
}
