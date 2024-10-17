package fr.traqueur.vaults.users;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class ZUser implements User {

    private final UUID uuid;
    private final String name;

    public ZUser(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public ZUser(Player player) {
        this(player.getUniqueId(), player.getName());
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void sendMessage(Message message, Formatter... formatters) {
        var player = this.getPlayer();
        if (player != null) {
            JavaPlugin.getPlugin(VaultsPlugin.class).getMessageResolver().sendMessage(player, message.translate(formatters));
        }
    }
}
