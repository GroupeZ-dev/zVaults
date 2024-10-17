package fr.traqueur.vaults.api.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class Placeholder {

    public static final Map<String, PlayerPlaceholder> PLACEHOLDER_MAP = new HashMap<>();
    public static final Map<String, RelationalPlaceholder> REL_PLACEHOLDER_MAP = new HashMap<>();

    public static void registerPlaceholder(Placeholder placeholder) {
        if (placeholder instanceof RelationalPlaceholder relationalPlaceholder) {
            REL_PLACEHOLDER_MAP.put(placeholder.identifier, relationalPlaceholder);
        } else if (placeholder instanceof PlayerPlaceholder placeholder1) {
            PLACEHOLDER_MAP.put(placeholder.identifier, placeholder1);
        }
    }

    public static String parse(Player player, String message) {
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }
        return message;
    }

    private final String identifier;

    public Placeholder(String identifier) {
        this.identifier = identifier;
    }

}