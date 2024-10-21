package fr.traqueur.vaults.api.events;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public class VaultOpenEvent extends VaultEvent implements Cancellable {

    private boolean cancelled;

    public VaultOpenEvent(VaultsPlugin plugin, @NotNull User who, Vault vault) {
        super(plugin, who, vault);
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
