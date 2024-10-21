package fr.traqueur.vaults.api.distributed;

import fr.traqueur.vaults.api.managers.Manager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;

public interface DistributedManager extends Manager {

    String CHANNEL_NAME = "vaults";

    void disable();

    void publishVaultUpdate(Vault vault, VaultItem item, int slot);

}
