package fr.traqueur.vaults.api.users;

import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface User {

    UUID getUniqueId();

    String getName();

    void sendMessage(Message message, Formatter... formatters);

    default boolean hasPermission(String permission) {
        return this.getPlayer().hasPermission(permission);
    }

    default Player getPlayer() {
        return Bukkit.getPlayer(this.getUniqueId());
    }

}
