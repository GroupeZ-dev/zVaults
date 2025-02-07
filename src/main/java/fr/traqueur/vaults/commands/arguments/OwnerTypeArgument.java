package fr.traqueur.vaults.commands.arguments;

import fr.traqueur.commands.api.arguments.ArgumentConverter;
import fr.traqueur.commands.api.arguments.TabCompleter;
import fr.traqueur.vaults.api.vaults.OwnerResolver;
import org.bukkit.command.CommandSender;

import java.util.List;

public class OwnerTypeArgument implements ArgumentConverter<String>, TabCompleter {

    private final OwnerResolver ownerResolver;

    public OwnerTypeArgument(OwnerResolver ownerResolver) {
        this.ownerResolver = ownerResolver;
    }

    @Override
    public String apply(String s) {
        return this.ownerResolver.isPresent(s) ? s : null;
    }

    @Override
    public List<String> onCompletion(CommandSender commandSender, List<String> args) {
        return this.ownerResolver.getOwnerTypes().keySet().stream().map(String::toUpperCase).toList();
    }
}
