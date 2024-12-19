package fr.traqueur.vaults.commands.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.users.ZUserManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class VaultArgument implements ArgumentConverter<Vault>, TabCompleter {

    private final VaultsManager vaultsManager;
    private final UserManager userManager;

    public VaultArgument(VaultsManager vaultsManager, UserManager userManager) {
        this.vaultsManager = vaultsManager;
        this.userManager = userManager;
    }

    @Override
    public Vault apply(String s) {
        UUID uuid = UUID.fromString(s);
        return this.vaultsManager.getVault(uuid);
    }

    @Override
    public List<String> onCompletion(CommandSender commandSender, List<String> args) {
        User user;
        String last = args.getLast();
        Player player = Bukkit.getPlayer(last);
        if(player != null) {
            user = this.userManager.getUser(player.getUniqueId()).orElse(ZUserManager.CONSOLE_USER);
        } else {
            user = ZUserManager.CONSOLE_USER;
        }
        if(user == ZUserManager.CONSOLE_USER) {
            return List.of();
        }
        return this.vaultsManager.getVaults(user)
                .stream()
                .map(vault -> vault.getUniqueId().toString())
                .toList();
    }
}
