package fr.traqueur.vaults.api.vaults;

import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class VaultOwner {

    private final UUID uniqueId;

    public VaultOwner(User user) {
        this.uniqueId = this.fromUser(user);
    }

    public VaultOwner(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public abstract UUID fromUser(User user);

    public abstract boolean hasAccess(Player player);

    public abstract void sendMessage(Message message);

}
