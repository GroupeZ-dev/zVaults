package fr.traqueur.vaults.api.distributed;

import fr.traqueur.vaults.api.events.VaultOpenEvent;
import fr.traqueur.vaults.api.managers.Manager;
import fr.traqueur.vaults.api.vaults.Vault;
import org.bukkit.inventory.ItemStack;

public interface DistributedManager extends Manager {

    String UPDATE_CHANNEL_NAME = "vaults";
    String OPEN_CHANNEL_NAME = "vaults-open";
    String OPEN_ACK_CHANNEL_NAME = "vaults-open-ack";

    void disable();

    void publishVaultUpdate(Vault vault, ItemStack item, int slot);

    void publishOpenRequest(VaultOpenEvent event);
}
