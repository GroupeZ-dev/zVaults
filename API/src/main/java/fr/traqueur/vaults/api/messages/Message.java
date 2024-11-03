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
    NO_REQUIREMENT_MESSAGE,
    MAX_VAULTS_REACHED,
    VAULT_CREATED,
    RECEIVE_NEW_VAULT,
    SIZE_NOT_AVAILABLE,
    VAULT_GROWED,
    VAULT_GROWED_SUCCESS,
    VAULT_NOT_FOUND,
    VAULT_SET_SIZE,
    VAULT_SET_SIZE_SUCCESS,
    NOT_PERMISSION_CONFIGURE_VAULT,
    ALREADY_ACCESS_TO_VAULT,
    SUCCESSFULLY_ADDED_ACCESS_TO_VAULT,
    SUCCESSFULLY_REMOVED_ACCESS_TO_VAULT, ITEM_CANT_BE_NULL, VAULT_ICON_CHANGE, VAULT_DELETED, RELOAD_PLUGIN_SUCCESS, SET_SIZE_COMMAND_DESCRIPTION, OPEN_COMMAND_DESCRIPTION, GROW_SIZE_COMMAND_DESCRIPTION, CREATE_COMMAND_DESCRIPTION, STACKLIMIT_COMMAND_DESCRIPTION, VAULT_NOT_INFINITE, STACKLIMIT_SET;

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