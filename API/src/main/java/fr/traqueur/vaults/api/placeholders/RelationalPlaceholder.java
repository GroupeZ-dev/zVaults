package fr.traqueur.vaults.api.placeholders;

import org.bukkit.entity.Player;

import java.util.List;

public abstract class RelationalPlaceholder extends Placeholder {

    public RelationalPlaceholder(String identifier) {
        super(identifier);
    }

    public abstract String onPlaceholderRequest(Player player, Player player2, List<String> params);
}