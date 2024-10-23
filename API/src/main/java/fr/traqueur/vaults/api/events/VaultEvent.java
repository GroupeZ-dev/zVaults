package fr.traqueur.vaults.api.events;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public abstract class VaultEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    private final VaultsPlugin plugin;
    private final Vault vault;

    public VaultEvent(VaultsPlugin plugin, @NotNull User who, Vault vault) {
        super(who.getPlayer());
        this.plugin = plugin;
        this.vault = vault;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public User getUser() {
        return this.plugin.getManager(UserManager.class).getUser(this.getPlayer().getUniqueId()).orElseThrow();
    }

    public Vault getVault() {
        return vault;
    }
}
