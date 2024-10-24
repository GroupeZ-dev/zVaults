package fr.traqueur.vaults.api.events;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.configurator.ShareType;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class VaultShareEvent extends VaultEvent{

    private final UUID sharedUniqueId;
    private final ShareType shareType;

    public VaultShareEvent(VaultsPlugin plugin, @NotNull User who, Vault vault, UUID sharedUniqueId, ShareType shareType) {
        super(plugin, who, vault);
        this.sharedUniqueId = sharedUniqueId;
        this.shareType = shareType;
    }

    public UUID getSharedUniqueId() {
        return this.sharedUniqueId;
    }

    public ShareType getShareType() {
        return this.shareType;
    }
}
