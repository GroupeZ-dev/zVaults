package fr.traqueur.vaults.api.events;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.vaults.Vault;

public class VaultDeleteEvent extends VaultEvent{

    public VaultDeleteEvent(VaultsPlugin plugin, Vault vault) {
        super(plugin, vault);
    }
}
