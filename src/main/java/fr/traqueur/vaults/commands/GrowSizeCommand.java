package fr.traqueur.vaults.commands;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import fr.traqueur.vaults.api.VaultsPlugin;
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

public class GrowSizeCommand extends Command<VaultsPlugin> {

    private final UserManager userManager;
    private final VaultsManager vaultsManager;

    // /zvaults growsize <player> <vault_num> <size>
    public GrowSizeCommand(VaultsPlugin plugin) {
        super(plugin, "growsize");

        this.userManager = plugin.getManager(UserManager.class);
        this.vaultsManager = plugin.getManager(VaultsManager.class);

        this.addArgs("receiver:user");
        this.addArgs("vault_num:int", (sender) -> this.vaultsManager.getNumVaultsTabulation());
        this.addOptinalArgs("size:int", (sender) -> List.of("9", "18", "27"));
        this.setGameOnly(true);
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        User user = this.userManager.getUser(((Player) commandSender).getUniqueId()).orElseThrow();
        User receiver = arguments.get("receiver");
        int size = arguments.get("size");
        int vaultNum = arguments.get("vault_num");
        if(size % 9 != 0) {
            user.sendMessage(Message.SIZE_NOT_AVAILABLE, Formatter.format("%size%", size));
            return;
        }
        Vault vault;
        try {
            vault = this.vaultsManager.getVault(receiver, vaultNum);
        } catch (IndexOutOfBoundVaultException e) {
            user.sendMessage(Message.VAULT_NOT_FOUND);
            return;
        }
        if(!this.vaultsManager.sizeIsAvailable(vault.getSize() + size)) {
            user.sendMessage(Message.SIZE_NOT_AVAILABLE, Formatter.format("%size%", vault.getSize() + size));
            return;
        }
        vault.setSize(vault.getSize() + size);
        vault.getOwner().sendMessage(Message.VAULT_GROWED, Formatter.format("%size%", vault.getSize()));
        user.sendMessage(Message.VAULT_GROWED_SUCCESS, Formatter.format("%size%", vault.getSize()));
    }
}
