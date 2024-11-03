package fr.traqueur.vaults.commands.admin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.commands.VCommand;
import fr.traqueur.vaults.api.exceptions.IndexOutOfBoundVaultException;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SetStackLimitCommand extends VCommand {

    private final UserManager userManager;
    private final VaultsManager vaultsManager;

    public SetStackLimitCommand(VaultsPlugin plugin) {
        super(plugin, "setstacklimit");

        this.userManager = plugin.getManager(UserManager.class);
        this.vaultsManager = plugin.getManager(VaultsManager.class);

        this.setPermission("zvaults.admin.setstacklimit");
        this.setUsage("/zvaults setstacklimit <player> <vault_num> <stacklimit>");
        this.setDescription(plugin.getMessageResolver().convertToLegacySectionFormat(Message.STACKLIMIT_COMMAND_DESCRIPTION.translate()));

        this.addArgs("receiver:user");
        this.addArgs("vault_num:int", (sender) -> this.vaultsManager.getNumVaultsTabulation());
        this.addOptinalArgs("stacklimit:int", (sender) -> List.of("64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768", "65536", "131072", "262144", "524288", "1048576", "2097152", "4194304", "8388608", "16777216", "33554432", "67108864", "134217728", "268435456", "536870912", "1073741824", "2147483647"));
        this.setGameOnly(true);
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        User user = this.userManager.getUser(((Player) commandSender).getUniqueId()).orElseThrow();
        User receiver = arguments.get("receiver");
        int size = arguments.get("stacklimit");
        int vaultNum = arguments.get("vault_num");
        Vault vault;
        try {
            vault = this.vaultsManager.getVault(receiver, vaultNum);
        } catch (IndexOutOfBoundVaultException e) {
            user.sendMessage(Message.VAULT_NOT_FOUND);
            return;
        }
        if(!vault.isInfinite()) {
            user.sendMessage(Message.VAULT_NOT_INFINITE);
            return;
        }
        vault.setMaxStackSize(size);
        user.sendMessage(Message.STACKLIMIT_SET, Formatter.format("%stacklimit%", size));
    }
}
