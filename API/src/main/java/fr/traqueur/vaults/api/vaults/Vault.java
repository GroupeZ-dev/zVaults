package fr.traqueur.vaults.api.vaults;

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

}
