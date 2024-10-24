package fr.traqueur.vaults.api.events;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import org.jetbrains.annotations.NotNull;

public class VaultUpdateEvent extends UserVaultEvent {

    private final VaultItem item;
    private final int slot;

    public VaultUpdateEvent(VaultsPlugin plugin, @NotNull User who, Vault vault, VaultItem item, int slot) {
        super(plugin, who, vault);
        this.item = item;
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public VaultItem getItem() {
        return item;
    }
}
