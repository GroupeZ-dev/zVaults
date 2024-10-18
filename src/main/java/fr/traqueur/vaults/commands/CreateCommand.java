package fr.traqueur.vaults.commands;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.commands.api.Command;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.VaultOwner;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class CreateCommand extends Command<VaultsPlugin> {

    private final UserManager userManager;
    private final VaultsManager vaultsManager;

    // /zvaults create <player> <size> (<type>)
    public CreateCommand(VaultsPlugin plugin) {
        super(plugin, "create");

        this.userManager = plugin.getManager(UserManager.class);
        this.vaultsManager = plugin.getManager(VaultsManager.class);

        this.addArgs("receiver:user");
        this.addArgs("size:int", (sender) -> this.vaultsManager.getSizeTabulation());
        this.addArgs("type:ownerType");
        this.setGameOnly(true);
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        User user = this.userManager.getUser(((Player) commandSender).getUniqueId()).orElseThrow();
        User receiver = arguments.get("receiver");
        int size = arguments.get("size");
        Optional<String> opt = arguments.getOptional("type");
        String type = opt.orElse("player");
        if (this.vaultsManager.sizeIsAvailable(size)) {
            user.sendMessage(Message.SIZE_NOT_AVAILABLE, Formatter.format("%size%", size));
            return;
        }
        VaultOwner owner = this.vaultsManager.generateOwner(type, receiver);
        this.vaultsManager.createVault(user, owner, size);
    }
}
