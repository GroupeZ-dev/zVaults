package fr.traqueur.vaults.api.placeholders;

import org.bukkit.entity.Player;

import java.util.List;

public abstract class PlayerPlaceholder extends Placeholder{

    public PlayerPlaceholder(String identifier) {
        super(identifier);
    }

    public abstract String onPlaceholderRequest(Player player, List<String> params);
}