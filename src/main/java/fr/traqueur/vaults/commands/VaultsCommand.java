package fr.traqueur.vaults.commands;

import fr.traqueur.commands.api.Arguments;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.commands.VaultCommand;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.MainConfiguration;
import fr.traqueur.vaults.api.exceptions.IndexOutOfBoundVaultException;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public class VaultsCommand extends VaultCommand {

    private final UserManager userManager;
    private final VaultsManager vaultsManager;

    public VaultsCommand(VaultsPlugin plugin) {
        super(plugin, "zvaults");

        this.userManager = plugin.getManager(UserManager.class);
        this.vaultsManager = plugin.getManager(VaultsManager.class);

        this.addSubCommand(new AdminCommand(plugin), new ReloadCommand(plugin));

        this.addAlias(Configuration.get(MainConfiguration.class).getAliases().toArray(new String[0]));

        this.addOptionalArgs("vault:int", (sender, args) -> List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));

        this.setGameOnly(true);
    }

    @Override
    public void execute(CommandSender commandSender, Arguments arguments) {
        User user = this.userManager.getUser(((Player) commandSender).getUniqueId()).orElseThrow();
        Optional<Integer> vault = arguments.getOptional("vault");
        if(vault.isPresent()) {
            try {
                Vault vaultToOpen = this.vaultsManager.getVault(user, vault.get());
                this.vaultsManager.openVault(user, vaultToOpen);
            } catch (IndexOutOfBoundVaultException ignored) {
               user.sendMessage(Message.VAULT_NOT_FOUND);
            }
        } else {
            this.vaultsManager.openVaultChooseMenu(user, user);
        }
    }
}
