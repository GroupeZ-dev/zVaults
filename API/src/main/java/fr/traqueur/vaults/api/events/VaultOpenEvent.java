package fr.traqueur.vaults.api.events;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VaultOpenEvent extends VaultEvent implements Cancellable {

    private boolean cancelled;

    private final List<VaultItem> content;

    public VaultOpenEvent(VaultsPlugin plugin, @NotNull User who, Vault vault) {
        super(plugin, who, vault);
        this.cancelled = false;
        this.content = vault.getContent();
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setContent(List<VaultItem> content) {
        this.content.clear();
        this.content.addAll(content);
    }

    public List<VaultItem> getContent() {
        return this.content;
    }


    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
