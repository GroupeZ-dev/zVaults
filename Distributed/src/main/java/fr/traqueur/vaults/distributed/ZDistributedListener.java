package fr.traqueur.vaults.distributed;

import fr.traqueur.vaults.api.distributed.DistributedManager;
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
        try {
            this.distributedManager.publishOpenRequest(event).handle((pubSub, throwable) -> {
                pubSub.unsubscribe();
                return null;
            }).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException ignored) {
        }
    }

    @EventHandler
    public void onVaultClose(VaultCloseEvent event) {

    }

    @EventHandler
    public void onVaultUpdate(VaultUpdateEvent event) {
        this.distributedManager.
                publishVaultUpdate(event.getVault(),
                        event.getItem().toItem(event.getUser().getPlayer(),
                                event.getVault().isInfinite()), event.getSlot());
    }

}