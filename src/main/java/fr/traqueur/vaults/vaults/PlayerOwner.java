package fr.traqueur.vaults.vaults;

import fr.traqueur.vaults.api.vaults.VaultOwner;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerOwner extends VaultOwner {

    public PlayerOwner(UUID uniqueId) {
        super(uniqueId);
    }

    @Override
    public boolean isOwner(Player player) {
        return player.getUniqueId().equals(this.getUniqueId());
    }
}
