package fr.traqueur.vaults.vaults;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.VaultOwner;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class ZPlayerOwner extends VaultOwner {


    public ZPlayerOwner(User user) {super(user);}

    public ZPlayerOwner(UUID uniqueId) {
        super(uniqueId);
    }

    @Override
    public UUID fromUser(User user) {
        return user.getUniqueId();
    }

    @Override
    public boolean hasAccess(UUID player) {
        return player.equals(this.getUniqueId());
    }

    @Override
    public void sendMessage(Message message, Formatter... formatters) {
        JavaPlugin.getPlugin(VaultsPlugin.class)
                .getManager(UserManager.class)
                .getUser(this.getUniqueId())
                .ifPresent(user -> user.sendMessage(message, formatters));
    }
}
