package fr.traqueur.vaults.commands.admin;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.commands.VaultCommand;
import fr.traqueur.vaults.api.exceptions.IndexOutOfBoundVaultException;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.users.ZUserManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class AddStackLimitCommand extends VaultCommand {

    private final UserManager userManager;
    private final VaultsManager vaultsManager;

    public AddStackLimitCommand(VaultsPlugin plugin) {
        super(plugin, "addstacklimit");

        this.userManager = plugin.getManager(UserManager.class);
        this.vaultsManager = plugin.getManager(VaultsManager.class);

        this.setPermission("zvaults.admin.addstacklimit");
        this.setUsage("/zvaults addstacklimit <player> <vault_num> <stacklimit>");
        this.setDescription(plugin.getMessageResolver().convertToLegacySectionFormat(Message.ADDSTACKLIMIT_COMMAND_DESCRIPTION.translate()));

        this.addArgs("receiver:user");
        this.addArgs("vault_num:int", (sender, args) -> this.vaultsManager.getNumVaultsTabulation());
        this.addOptionalArgs("stacklimit:int", (sender,args) -> List.of("64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768", "65536", "131072", "262144", "524288", "1048576", "2097152", "4194304", "8388608", "16777216", "33554432", "67108864", "134217728", "268435456", "536870912", "1073741824", "2147483647"));
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
        int maxStackSize = Math.max(1, vault.getMaxStackSize() + size);
        vault.setMaxStackSize(maxStackSize);
        user.sendMessage(Message.STACKLIMIT_ADD, Formatter.format("%stacklimit%", size));
    }
}
