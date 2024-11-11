package fr.traqueur.vaults.commands.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import org.bukkit.command.CommandSender;

import java.util.List;

public class UserArgument implements ArgumentConverter<User>, TabCompleter {

    private final UserManager userManager;

    public UserArgument(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public User apply(String s) {
        return this.userManager.getUser(s).orElse(null);
    }

    @Override
    public List<String> onCompletion(CommandSender commandSender, List<String> args) {
        return this.userManager.getUsers().stream().map(User::getName).toList();
    }
}
