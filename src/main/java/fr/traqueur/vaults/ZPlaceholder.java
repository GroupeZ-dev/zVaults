package fr.traqueur.vaults;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.placeholders.Placeholder;
import fr.traqueur.vaults.api.placeholders.PlayerPlaceholder;
import fr.traqueur.vaults.api.placeholders.RelationalPlaceholder;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ZPlaceholder extends PlaceholderExpansion implements Relational {

    private final VaultsPlugin plugin;

    public ZPlaceholder(VaultsPlugin plugin) {
        this.plugin = plugin;
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "zvaults";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "GroupeZ";
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Nullable
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        for (Map.Entry<String, PlayerPlaceholder> stringPlayerPlaceholderEntry : Placeholder.PLACEHOLDER_MAP.entrySet()) {
            String identifier = stringPlayerPlaceholderEntry.getKey();
            PlayerPlaceholder placeholder = stringPlayerPlaceholderEntry.getValue();
            List<String> list = new ArrayList<>();
            if(this.isRegister(params, identifier, list)) {
                return placeholder.onPlaceholderRequest(player, list);
            }
        }

        return "Error";
    }

    @Override
    public String onPlaceholderRequest(Player player, Player player1, String params) {
        for (Map.Entry<String, RelationalPlaceholder> stringPlayerPlaceholderEntry : Placeholder.REL_PLACEHOLDER_MAP.entrySet()) {
            String identifier = stringPlayerPlaceholderEntry.getKey();
            RelationalPlaceholder placeholder = stringPlayerPlaceholderEntry.getValue();
            List<String> list = new ArrayList<>();
            if(this.isRegister(params, identifier, list)) {
                return placeholder.onPlaceholderRequest(player, player1, list);
            }
        }

        return "Error";
    }

    private boolean isRegister(String params, String identifier, List<String> list) {
        if (params.startsWith(identifier + "_")) {
            list.addAll(List.of(params.replace(identifier+"_", "").split("_")));
        } else if (params.startsWith(identifier)) {
            list.addAll(List.of(params.replace(identifier, "").split("_")));
        } else {
            return false;
        }
        return true;
    }
}