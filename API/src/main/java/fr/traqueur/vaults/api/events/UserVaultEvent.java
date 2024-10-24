package fr.traqueur.vaults.api.events;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class UserVaultEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final VaultsPlugin plugin;
    private final Vault vault;
    private final User user;

    public UserVaultEvent(VaultsPlugin plugin, @NotNull User who, Vault vault) {
        this.plugin = plugin;
        this.vault = vault;
        this.user = who;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public User getUser() {
        return user;
    }

    public Vault getVault() {
        return vault;
    }
}
