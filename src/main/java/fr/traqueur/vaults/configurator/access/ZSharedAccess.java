package fr.traqueur.vaults.configurator.access;

import fr.traqueur.vaults.api.configurator.SharedAccess;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;

import java.util.UUID;

public class ZSharedAccess implements SharedAccess {

    private final UUID uniqueId;
    private final Vault vault;
    private final User user;

    public ZSharedAccess(UUID uniqueId, Vault vault, User user) {
        this.uniqueId = uniqueId;
        this.vault = vault;
        this.user = user;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public Vault getVault() {
        return this.vault;
    }

    @Override
    public User getUser() {
        return this.user;
    }
}
