package fr.traqueur.vaults.api.placeholders;

import fr.traqueur.vaults.api.Plugins;
import fr.traqueur.vaults.api.VaultsPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Placeholders {

    private static boolean usePAPI;
    private static PlaceholdersManager placeholdersManager;

    public static void load(VaultsPlugin plugin) {
        usePAPI = Plugins.PAPI.isEnable();
        placeholdersManager = new PlaceholdersManager(plugin.getName());
        if(usePAPI) {
            PlaceholdersHook placeholdersHook = new PlaceholdersHook(plugin, placeholdersManager);
            placeholdersHook.register();
        }
    }

    public static void register(String identifier, BiFunction<Player, List<String>,String> function) {
        placeholdersManager.register(identifier, function);
    }

    public static void register(String identifier, TriFunction<Player, Player,List<String>, String> function) {
        placeholdersManager.register(identifier, function);
    }

    public static String parse(Player player, String text) {
        if(usePAPI) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return placeholdersManager.setPlaceholders(player, text);
    }

    public static String parse(Player player, Player player2, String text) {
        if(usePAPI) {
            return PlaceholderAPI.setRelationalPlaceholders(player, player2, text);
        }
        return placeholdersManager.setRelationalPlaceholders(player, player2, text);
    }

    public static List<String> parse(Player player, List<String> text) {
        if(usePAPI) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text.stream().map(s -> placeholdersManager.setPlaceholders(player, s)).collect(Collectors.toList());
    }

    public static List<String> parse(Player player, Player player2, List<String> text) {
        if(usePAPI) {
            return PlaceholderAPI.setRelationalPlaceholders(player, player2, text);
        }
        return text.stream().map(s -> placeholdersManager.setRelationalPlaceholders(player, player2, s)).collect(Collectors.toList());
    }

}
