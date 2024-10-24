package fr.traqueur.vaults.distributed;

import fr.traqueur.vaults.api.distributed.DistributedManager;
import fr.traqueur.vaults.api.distributed.requests.VaultShareRequest;
import fr.traqueur.vaults.api.distributed.requests.VaultStateRequest;
import fr.traqueur.vaults.api.events.*;
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
        if(this.distributedManager.isOpenGlobal(event.getVault())) {
            event.setSave(false);
        } else {
            this.distributedManager.publishCloseRequest(event.getVault());
        }
    }

    @EventHandler
    public void onCreate(VaultCreateEvent event) {
        this.distributedManager.publishCreateRequest(event.getVault());
    }

    @EventHandler
    public void onShare(VaultShareEvent event) {
        this.distributedManager.publishShareRequest(event);
    }

    @EventHandler
    public void onVaultUpdate(VaultUpdateEvent event) {
        this.distributedManager.
                publishVaultUpdate(event.getVault(),
                        event.getItem(), event.getSlot());
    }

    @EventHandler
    public void onVaultShareRequest(VaultChangeSizeEvent event) {
        this.distributedManager.publishSizeChangeRequest(event.getVault());
    }

    @EventHandler
    public void onChangeIcon(VaultChangeIconEvent event) {
        this.distributedManager.publishIconChangeRequest(event.getVault(), event.getMaterial());
    }


}
