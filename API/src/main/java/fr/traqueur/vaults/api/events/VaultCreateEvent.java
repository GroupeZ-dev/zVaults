package fr.traqueur.vaults.api.events;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;
import org.jetbrains.annotations.NotNull;

public class VaultCreateEvent extends UserVaultEvent {

    public VaultCreateEvent(VaultsPlugin plugin, @NotNull User who, Vault vault) {
        super(plugin, who, vault);
    }
}
