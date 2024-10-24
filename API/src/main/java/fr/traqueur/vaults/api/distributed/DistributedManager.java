package fr.traqueur.vaults.api.distributed;

import fr.traqueur.vaults.api.distributed.requests.VaultStateRequest;
import fr.traqueur.vaults.api.events.VaultOpenEvent;
import fr.traqueur.vaults.api.managers.Manager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.CompletableFuture;

public interface DistributedManager extends Manager {

    String UPDATE_CHANNEL_NAME = "vaults";
    String OPEN_CHANNEL_NAME = "vaults-open";
    String OPEN_ACK_CHANNEL_NAME = "vaults-open-ack";
    String STATE_CHANNEL_NAME = "vaults-state";

    void disable();

    void publishVaultUpdate(Vault vault, VaultItem item, int slot);

    boolean isOpenGlobal(Vault vault);

    CompletableFuture<JedisPubSub> publishOpenRequest(VaultOpenEvent event);

    void publishStateRequest(Vault vault, VaultStateRequest.State state);
}
