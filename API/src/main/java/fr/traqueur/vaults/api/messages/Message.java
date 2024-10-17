package fr.traqueur.vaults.api.messages;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.LangConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public enum Message {

    NO_PERMISSION_MESSAGE,
    ONLY_IN_GAME_MESSAGE,
    MISSING_ARGS_MESSAGE,
    ARG_NOT_RECOGNIZED_MESSAGE,
    NO_REQUIREMENT_MESSAGE;

    public String translate(Formatter... formatters) {
        return translate(false, formatters);
    }

    public String translate(boolean legacy, Formatter... formatters) {
        VaultsPlugin plugin = JavaPlugin.getPlugin(VaultsPlugin.class);

        String message = Configuration.getConfiguration(LangConfiguration.class).translate(this);
        for (Formatter formatter : formatters) {
            message = formatter.handle(plugin, message);
        }
        if(!legacy) return message;
        return plugin.getMessageResolver().convertToLegacyFormat(message);
    }

}