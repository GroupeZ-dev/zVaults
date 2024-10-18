package fr.traqueur.vaults.api.vaults;

import fr.traqueur.vaults.api.users.User;

import java.util.List;
import java.util.UUID;

public interface Vault {

    UUID getUniqueId();

    VaultOwner getOwner();

    int getSize();

    void setSize(int size);

    List<VaultItem> getContent();

    void setContent(List<VaultItem> content);

    boolean isInfinite();

    default boolean hasAccess(User receiver) {
        return this.getOwner().hasAccess(receiver.getUniqueId());
    }
}
