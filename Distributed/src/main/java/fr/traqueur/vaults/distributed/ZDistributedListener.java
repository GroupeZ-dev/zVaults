package fr.traqueur.vaults.distributed;

import fr.traqueur.vaults.api.distributed.DistributedManager;
import fr.traqueur.vaults.api.distributed.VaultUpdate;
import fr.traqueur.vaults.api.events.VaultCloseEvent;
import fr.traqueur.vaults.api.events.VaultOpenEvent;
import fr.traqueur.vaults.api.events.VaultUpdateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ZDistributedListener implements Listener {

    private final DistributedManager distributedManager;

    public ZDistributedListener(DistributedManager distributedManager) {
        this.distributedManager = distributedManager;
    }

    @EventHandler
    public void onVaultOpen(VaultOpenEvent event) {

    }

    @EventHandler
    public void onVaultClose(VaultCloseEvent event) {

    }

    @EventHandler
    public void onVaultUpdate(VaultUpdateEvent event) {
        this.distributedManager.publishVaultUpdate(event.getVault(), event.getItem(), event.getSlot());
    }

}
