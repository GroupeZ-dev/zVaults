package fr.traqueur.vaults.commands.admin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.commands.VaultCommand;
import fr.traqueur.vaults.api.exceptions.IndexOutOfBoundVaultException;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SetSizeCommand extends VaultCommand {

    private final UserManager userManager;
    private final VaultsManager vaultsManager;

    // /zvaults setsize <player> <vault_num> <size>
    public SetSizeCommand(VaultsPlugin plugin) {
        super(plugin, "setsize");

        this.userManager = plugin.getManager(UserManager.class);
        this.vaultsManager = plugin.getManager(VaultsManager.class);

        this.setPermission("zvaults.admin.setsize");
        this.setUsage("/zvaults setsize <player> <vault_num> <size>");
        this.setDescription(plugin.getMessageResolver().convertToLegacySectionFormat(Message.SET_SIZE_COMMAND_DESCRIPTION.translate()));

        this.addArgs("receiver:user");
        this.addArgs("vault_num:int", (sender, args) -> this.vaultsManager.getNumVaultsTabulation());
        this.addOptionalArgs("size:int", (sender, args) -> List.of("9", "18", "27"));
        this.setGameOnly(true);
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        User user = this.userManager.getUser(((Player) commandSender).getUniqueId()).orElseThrow();
        User receiver = arguments.get("receiver");
        int size = arguments.get("size");
        int vaultNum = arguments.get("vault_num");
        Vault vault;
        try {
            vault = this.vaultsManager.getVault(receiver, vaultNum);
        } catch (IndexOutOfBoundVaultException e) {
            user.sendMessage(Message.VAULT_NOT_FOUND);
            return;
        }
        this.vaultsManager.changeSizeOfVault(user, vault, size, Message.VAULT_SET_SIZE_SUCCESS, Message.VAULT_SET_SIZE);
    }
}
