package fr.traqueur.vaults.api.events;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;
import org.jetbrains.annotations.NotNull;

public class VaultCloseEvent extends VaultEvent {

    private boolean save;

    public VaultCloseEvent(VaultsPlugin plugin, @NotNull User who, Vault vault) {
        super(plugin, who, vault);
        this.save = true;
    }

    public boolean isSave() {
        return this.save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }
}
