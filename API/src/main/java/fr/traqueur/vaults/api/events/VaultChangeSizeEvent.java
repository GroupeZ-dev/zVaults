package fr.traqueur.vaults.api.events;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.vaults.Vault;

public class VaultChangeSizeEvent extends VaultEvent {

    private final int newSize;

    public VaultChangeSizeEvent(VaultsPlugin plugin, Vault vault, int newSize) {
        super(plugin, vault);
        this.newSize = newSize;
    }

    public int getNewSize() {
        return newSize;
    }
}
