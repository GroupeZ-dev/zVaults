package fr.traqueur.vaults.api.vaults;

import org.bukkit.entity.Player;

import java.util.UUID;

public abstract class VaultOwner {

    private final UUID uniqueId;

    public VaultOwner(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public abstract boolean hasAccess(Player player);

}
