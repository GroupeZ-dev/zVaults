package fr.traqueur.vaults.distributed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.traqueur.vaults.api.VaultsLogger;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.MainConfiguration;
import fr.traqueur.vaults.api.distributed.DistributedManager;
import fr.traqueur.vaults.api.distributed.RedisConnectionConfig;
import fr.traqueur.vaults.api.distributed.requests.VaultUpdateRequest;
import fr.traqueur.vaults.api.distributed.requests.VaultOpenAckRequest;
import fr.traqueur.vaults.api.distributed.requests.VaultOpenRequest;
import fr.traqueur.vaults.api.events.VaultOpenEvent;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.distributed.adapter.VaultOpenAckRequestAdapter;
import fr.traqueur.vaults.distributed.adapter.VaultUpdateAdapter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ZDistributedManager implements DistributedManager {

    private final ExecutorService executorService;
    private final Gson gson;
    private final UUID serverUUID;
    private final VaultsManager vaultsManager;


    public ZDistributedManager(VaultsPlugin plugin) {
        this.serverUUID = UUID.randomUUID();

        this.executorService = Executors.newCachedThreadPool();

        this.vaultsManager = plugin.getManager(VaultsManager.class);

        this.gson = new GsonBuilder()
                .registerTypeAdapter(VaultUpdateRequest.class, new VaultUpdateAdapter(plugin.getManager(VaultsManager.class)))
                .registerTypeAdapter(VaultOpenAckRequest.class, new VaultOpenAckRequestAdapter())
                .create();

        this.executorService.submit(this::subscribe);
        plugin.getServer().getPluginManager().registerEvents(new ZDistributedListener(this), plugin);
    }

    @Override
    public void disable() {
        this.executorService.shutdownNow();
    }

    @Override
    public void publishVaultUpdate(Vault vault, VaultItem item, int slot) {
        VaultUpdateRequest vaultUpdate = new VaultUpdateRequest(serverUUID, vault, item, slot);
        try (Jedis publisher = this.createJedisInstance(Configuration.getConfiguration(MainConfiguration.class).getRedisConnectionConfig())) {
            publisher.publish(UPDATE_CHANNEL_NAME, gson.toJson(vaultUpdate, VaultUpdateRequest.class));
        }
    }

    @Override
    public CompletableFuture<JedisPubSub> publishOpenRequest(VaultOpenEvent event) {
        CompletableFuture<JedisPubSub> completableFuture = new CompletableFuture<>();

        this.executorService.submit(() -> {
            try (Jedis openAckSubscriber = this.createJedisInstance(Configuration.getConfiguration(MainConfiguration.class).getRedisConnectionConfig())) {
                openAckSubscriber.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        if (channel.equals(OPEN_ACK_CHANNEL_NAME)) {
                            VaultOpenAckRequest request = gson.fromJson(message, VaultOpenAckRequest.class);
                            if (request.server().equals(serverUUID) && event.getVault().getUniqueId().equals(request.vault())) {
                                if (Configuration.getConfiguration(MainConfiguration.class).isDebug()) {
                                    VaultsLogger.info("Received open ack for vault " + request.vault());
                                }
                                event.setContent(request.items());
                                completableFuture.complete(this);
                            }
                        }
                    }
                }, OPEN_ACK_CHANNEL_NAME);
            }
        });

        VaultOpenRequest openRequest = new VaultOpenRequest(serverUUID, event.getVault().getUniqueId());
        try (Jedis publisher = this.createJedisInstance(Configuration.getConfiguration(MainConfiguration.class).getRedisConnectionConfig())) {
            if(Configuration.getConfiguration(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Sending open request for vault " + event.getVault().getUniqueId());
            }
            publisher.publish(OPEN_CHANNEL_NAME, gson.toJson(openRequest, VaultOpenRequest.class));
        }
        return completableFuture;
    }

    private void handleVaultUpdate(VaultUpdateRequest vaultUpdate) {
        this.vaultsManager.getLinkedInventory(vaultUpdate.vault().getUniqueId()).ifPresent(inventory -> {
            if (Configuration.getConfiguration(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Received vault update for vault " + vaultUpdate.vault().getUniqueId() + " on slot " + vaultUpdate.slot());
            }
            vaultUpdate.vault().setContent(vaultUpdate.vault().getContent().stream().map(item -> item.slot() == vaultUpdate.slot() ? vaultUpdate.itemStack() : item).collect(Collectors.toList()));
            inventory.getSpigotInventory().setItem(vaultUpdate.slot(), vaultUpdate.itemStack().toItem(null, vaultUpdate.vault().isInfinite()));
        });
    }

    private void handleVaultOpen(VaultOpenRequest openRequest) {
        this.vaultsManager.getLinkedInventory(openRequest.vaultId()).ifPresent(inventory -> {
            if (Configuration.getConfiguration(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Received open request for vault " + openRequest.vaultId());
            }
            Vault vault = this.vaultsManager.getVault(openRequest.vaultId());
            var request = new VaultOpenAckRequest(openRequest.server(), openRequest.vaultId(), vault.getContent());
            if (Configuration.getConfiguration(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Sending open ack for vault " + openRequest.vaultId());
            }
            try (Jedis ackPublisher = this.createJedisInstance(Configuration.getConfiguration(MainConfiguration.class).getRedisConnectionConfig())) {
                ackPublisher.publish(OPEN_ACK_CHANNEL_NAME, gson.toJson(request, VaultOpenAckRequest.class));
            }
        });
    }


    private void subscribe() {
        try (Jedis subscriber = this.createJedisInstance(Configuration.getConfiguration(MainConfiguration.class).getRedisConnectionConfig())) {
            subscriber.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    if (channel.equals(UPDATE_CHANNEL_NAME)) {
                        VaultUpdateRequest vaultUpdate = gson.fromJson(message, VaultUpdateRequest.class);
                        if(!vaultUpdate.server().equals(serverUUID)) {
                            handleVaultUpdate(vaultUpdate);
                        }
                    } else if (channel.equals(OPEN_CHANNEL_NAME)) {
                        VaultOpenRequest openRequest = gson.fromJson(message, VaultOpenRequest.class);
                        if(!openRequest.server().equals(serverUUID)) {
                            handleVaultOpen(openRequest);
                        }
                    }
                }
            }, UPDATE_CHANNEL_NAME, OPEN_CHANNEL_NAME);
        }
    }

    private Jedis createJedisInstance(RedisConnectionConfig config) {
        Jedis jedis = new Jedis(config.host(), config.port());
        if(config.password() != null && !config.password().isEmpty() && (config.user() == null || config.user().isEmpty())) {
            jedis.auth(config.password());
        } else if(config.user() != null && !config.user().isEmpty() && config.password() != null && !config.password().isEmpty()) {
            jedis.auth(config.user(), config.password());
        }
        return jedis;
    }

}
