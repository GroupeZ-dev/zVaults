package fr.traqueur.vaults.distributed;

import fr.traqueur.vaults.api.distributed.DistributedManager;
import fr.traqueur.vaults.api.distributed.requests.VaultStateRequest;
import fr.traqueur.vaults.api.events.VaultCloseEvent;
import fr.traqueur.vaults.api.events.VaultOpenEvent;
import fr.traqueur.vaults.api.events.VaultUpdateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ZDistributedListener implements Listener {

    private final DistributedManager distributedManager;

    public ZDistributedListener(DistributedManager distributedManager) {
        this.distributedManager = distributedManager;
    }

    @EventHandler
    public void onVaultOpen(VaultOpenEvent event) {
        this.distributedManager.publishStateRequest(event.getVault(), VaultStateRequest.State.OPEN);
        if(!this.distributedManager.isOpenGlobal(event.getVault())) {
            return;
        }

        try {
            this.distributedManager.publishOpenRequest(event).handle((pubSub, throwable) -> {
                pubSub.unsubscribe();
                return null;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void onVaultClose(VaultCloseEvent event) {
        this.distributedManager.publishStateRequest(event.getVault(), VaultStateRequest.State.CLOSE);
    }

    @EventHandler
    public void onVaultUpdate(VaultUpdateEvent event) {
        this.distributedManager.
                publishVaultUpdate(event.getVault(),
                        event.getItem(), event.getSlot());
    }

}
