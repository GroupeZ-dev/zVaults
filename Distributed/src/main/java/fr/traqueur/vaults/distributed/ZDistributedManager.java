package fr.traqueur.vaults.distributed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.traqueur.vaults.api.VaultsLogger;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.MainConfiguration;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.distributed.DistributedManager;
import fr.traqueur.vaults.api.distributed.RedisConnectionConfig;
import fr.traqueur.vaults.api.distributed.requests.*;
import fr.traqueur.vaults.api.events.VaultOpenEvent;
import fr.traqueur.vaults.api.events.VaultShareEvent;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.distributed.adapter.*;
import org.bukkit.Material;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;
import java.util.Map;
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
    private final Map<UUID, Integer> vaultStates;


    public ZDistributedManager(VaultsPlugin plugin) {
        this.serverUUID = UUID.randomUUID();

        this.vaultStates = new HashMap<>();

        this.executorService = Executors.newCachedThreadPool();

        this.vaultsManager = plugin.getManager(VaultsManager.class);

        this.gson = new GsonBuilder()
                .registerTypeAdapter(VaultUpdateRequest.class, new VaultUpdateAdapter(plugin.getManager(VaultsManager.class)))
                .registerTypeAdapter(VaultOpenAckRequest.class, new VaultOpenAckRequestAdapter())
                .registerTypeAdapter(VaultStateRequest.class, new VaultStateRequestAdapter())
                .registerTypeAdapter(VaultCloseRequest.class, new VaultCloseRequestAdapter())
                .registerTypeAdapter(VaultShareRequest.class, new VaultShareRequestAdapter())
                .registerTypeAdapter(VaultChangeSizeRequest.class, new VaultChangeSizeRequestAdapter())
                .registerTypeAdapter(VaultIconChangeRequest.class, new VaultChangeIconRequestAdapter())
                .registerTypeAdapter(VaultDeleteRequest.class, new VaultDeleteRequestAdapter())
                .create();

        this.executorService.submit(this::subscribe);
        plugin.getServer().getPluginManager().registerEvents(new ZDistributedListener(this), plugin);
    }

    @Override
    public void disable() {
        this.executorService.shutdownNow();
    }

    @Override
    public boolean isOpenGlobal(Vault vault) {
        return this.vaultStates.getOrDefault(vault.getUniqueId(), 0) > 0;
    }

    @Override
    public void publishVaultUpdate(Vault vault, VaultItem item, int slot) {
        VaultUpdateRequest vaultUpdate = new VaultUpdateRequest(serverUUID, vault, item, slot);
        try (Jedis publisher = this.createJedisInstance(Configuration.get(MainConfiguration.class).getRedisConnectionConfig())) {
            publisher.publish(UPDATE_CHANNEL_NAME, gson.toJson(vaultUpdate, VaultUpdateRequest.class));
        }
    }

    @Override
    public CompletableFuture<JedisPubSub> publishOpenRequest(VaultOpenEvent event) {
        CompletableFuture<JedisPubSub> completableFuture = new CompletableFuture<>();

        this.executorService.submit(() -> {
            try (Jedis openAckSubscriber = this.createJedisInstance(Configuration.get(MainConfiguration.class).getRedisConnectionConfig())) {
                openAckSubscriber.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        if (channel.equals(OPEN_ACK_CHANNEL_NAME)) {
                            VaultOpenAckRequest request = gson.fromJson(message, VaultOpenAckRequest.class);
                            if (request.server().equals(serverUUID) && event.getVault().getUniqueId().equals(request.vault())) {
                                if (Configuration.get(MainConfiguration.class).isDebug()) {
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
        try (Jedis publisher = this.createJedisInstance(Configuration.get(MainConfiguration.class).getRedisConnectionConfig())) {
            if(Configuration.get(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Sending open request for vault " + event.getVault().getUniqueId());
            }
            publisher.publish(OPEN_CHANNEL_NAME, gson.toJson(openRequest, VaultOpenRequest.class));
        }
        return completableFuture;
    }

    @Override
    public void publishStateRequest(Vault vault, VaultStateRequest.State state) {
        VaultStateRequest request = new VaultStateRequest(serverUUID, vault.getUniqueId(), state);
        try (Jedis publisher = this.createJedisInstance(Configuration.get(MainConfiguration.class).getRedisConnectionConfig())) {
            publisher.publish(STATE_CHANNEL_NAME, gson.toJson(request, VaultStateRequest.class));
        }
    }

    @Override
    public void publishCloseRequest(Vault vault) {
        try (Jedis publisher = this.createJedisInstance(Configuration.get(MainConfiguration.class).getRedisConnectionConfig())) {
            if(Configuration.get(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Sending close request for vault " + vault.getUniqueId());
            }
            VaultCloseRequest closeRequest = new VaultCloseRequest(serverUUID, vault.getUniqueId(), vault.getContent());
            publisher.publish(CLOSE_CHANNEL_NAME, gson.toJson(closeRequest, VaultCloseRequest.class));
        }
    }

    @Override
    public void publishCreateRequest(Vault vault) {
        try (Jedis publisher = this.createJedisInstance(Configuration.get(MainConfiguration.class).getRedisConnectionConfig())) {
            if (Configuration.get(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Sending create request for vault " + vault.getUniqueId());
            }
            VaultCreateRequest createRequest = new VaultCreateRequest(serverUUID, vault.getUniqueId(), this.vaultsManager.getOwnerResolver().getType(vault.getOwner().getClass()), vault.getOwner().getUniqueId(), vault.getSize(), vault.isInfinite(), vault.getId());
            publisher.publish(CREATE_CHANNEL_NAME, gson.toJson(createRequest, VaultCreateRequest.class));
        }
    }

    @Override
    public void publishShareRequest(VaultShareEvent event) {
        try (Jedis publisher = this.createJedisInstance(Configuration.get(MainConfiguration.class).getRedisConnectionConfig())) {
            if (Configuration.get(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Sending share request for vault " + event.getVault().getUniqueId());
            }
            VaultShareRequest shareRequest = new VaultShareRequest(serverUUID, event.getSharedUniqueId(), event.getVault().getUniqueId(), event.getUser().getUniqueId(), event.getShareType());
            publisher.publish(SHARE_CHANNEL_NAME, gson.toJson(shareRequest, VaultShareRequest.class));
        }
    }

    @Override
    public void publishSizeChangeRequest(Vault vault) {
        try (Jedis publisher = this.createJedisInstance(Configuration.get(MainConfiguration.class).getRedisConnectionConfig())) {
            if (Configuration.get(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Sending size change request for vault " + vault.getUniqueId());
            }
            VaultChangeSizeRequest sizeRequest = new VaultChangeSizeRequest(serverUUID, vault.getUniqueId(), vault.getSize());
            publisher.publish(SIZE_CHANNEL_NAME, gson.toJson(sizeRequest, VaultChangeSizeRequest.class));
        }
    }

    @Override
    public void publishIconChangeRequest(Vault vault, Material material) {
        try (Jedis publisher = this.createJedisInstance(Configuration.get(MainConfiguration.class).getRedisConnectionConfig())) {
            if (Configuration.get(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Sending icon change request for vault " + vault.getUniqueId());
            }
            VaultIconChangeRequest iconRequest = new VaultIconChangeRequest(serverUUID, vault.getUniqueId(), material);
            publisher.publish(ICON_CHANNEL_NAME, gson.toJson(iconRequest, VaultIconChangeRequest.class));
        }
    }

    @Override
    public void publishVaultDeleteRequest(Vault vault) {
        try (Jedis publisher = this.createJedisInstance(Configuration.get(MainConfiguration.class).getRedisConnectionConfig())) {
            if (Configuration.get(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Sending delete request for vault " + vault.getUniqueId());
            }
            VaultDeleteRequest deleteRequest = new VaultDeleteRequest(serverUUID, vault.getUniqueId());
            publisher.publish(DELETE_CHANNEL_NAME, gson.toJson(deleteRequest, VaultDeleteRequest.class));
        }
    }

    private void handleVaultUpdate(VaultUpdateRequest vaultUpdate) {
        this.vaultsManager.getLinkedInventory(vaultUpdate.vault().getUniqueId()).ifPresent(inventory -> {
            if (Configuration.get(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Received vault update for vault " + vaultUpdate.vault().getUniqueId() + " on slot " + vaultUpdate.slot());
            }
            vaultUpdate.vault().setContent(vaultUpdate.vault().getContent().stream().map(item -> item.slot() == vaultUpdate.slot() ? vaultUpdate.itemStack() : item).collect(Collectors.toList()));
            inventory.getSpigotInventory().setItem(vaultUpdate.slot(), vaultUpdate.itemStack().toItem(null, vaultUpdate.vault().isInfinite()));
        });
    }

    private void handleVaultOpen(VaultOpenRequest openRequest) {
        this.vaultsManager.getLinkedInventory(openRequest.vaultId()).ifPresent(inventory -> {
            if (Configuration.get(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Received open request for vault " + openRequest.vaultId());
            }
            Vault vault = this.vaultsManager.getVault(openRequest.vaultId());
            var request = new VaultOpenAckRequest(openRequest.server(), openRequest.vaultId(), vault.getContent());
            if (Configuration.get(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Sending open ack for vault " + openRequest.vaultId());
            }
            try (Jedis ackPublisher = this.createJedisInstance(Configuration.get(MainConfiguration.class).getRedisConnectionConfig())) {
                ackPublisher.publish(OPEN_ACK_CHANNEL_NAME, gson.toJson(request, VaultOpenAckRequest.class));
            }
        });
    }


    private void handleVaultStateChange(VaultStateRequest stateRequest) {
        int state = this.vaultStates.getOrDefault(stateRequest.vault(), 0);
        switch (stateRequest.state()) {
            case OPEN -> {
                this.vaultStates.put(stateRequest.vault(), state + 1);
            }
            case CLOSE -> {
                this.vaultStates.put(stateRequest.vault(), state - 1);
            }
        }
    }


    private void handleVaultCloseRequest(VaultCloseRequest closeRequest) {
        if (Configuration.get(MainConfiguration.class).isDebug()) {
            VaultsLogger.info("Received close request for vault " + closeRequest.vault());
        }
        this.vaultsManager.getVault(closeRequest.vault()).setContent(closeRequest.content());
    }

    private void handleVaultCreateRequest(VaultCreateRequest createRequest) {
        if (Configuration.get(MainConfiguration.class).isDebug()) {
            VaultsLogger.info("Received create request for vault " + createRequest.vault());
        }
        var owner = this.vaultsManager.getOwnerResolver().resolveOwner(createRequest.ownerType(), createRequest.owner());
        this.vaultsManager.createVault(createRequest.vault(), owner, createRequest.size(), createRequest.infinite(), createRequest.id());
    }

    private void handleVaultShareRequest(VaultShareRequest shareRequest) {
        if (Configuration.get(MainConfiguration.class).isDebug()) {
            VaultsLogger.info("Received share request for vault " + shareRequest.vault());
        }
        var vaultConfigManager = this.getPlugin().getManager(VaultConfigurationManager.class);
        switch (shareRequest.type()) {
            case SHARE -> {
                var user = this.getPlugin().getManager(UserManager.class).getUser(shareRequest.player()).orElseThrow();
                var vault = this.vaultsManager.getVault(shareRequest.vault());
                vaultConfigManager.addSharedAccess(shareRequest.shared(),user, vault);
            }
            case UNSHARE -> {
                vaultConfigManager.deleteSharedAccess(shareRequest.shared());
            }
        }
    }

    private void handleVaultSizeChange(VaultChangeSizeRequest sizeRequest) {
        if (Configuration.get(MainConfiguration.class).isDebug()) {
            VaultsLogger.info("Received size change request for vault " + sizeRequest.vault());
        }
        this.vaultsManager.getVault(sizeRequest.vault()).setSize(sizeRequest.size());
    }

    private void handleVaultIconChange(VaultIconChangeRequest iconRequest) {
        if (Configuration.get(MainConfiguration.class).isDebug()) {
            VaultsLogger.info("Received icon change request for vault " + iconRequest.vault());
        }
        this.vaultsManager.getVault(iconRequest.vault()).setIcon(iconRequest.icon());
    }

    private void handleDeleteRequest(VaultDeleteRequest deleteRequest) {
        if (Configuration.get(MainConfiguration.class).isDebug()) {
            VaultsLogger.info("Received delete request for vault " + deleteRequest.vaultId());
        }
        Vault vault = this.vaultsManager.getVault(deleteRequest.vaultId());
        this.vaultsManager.deleteVault(vault, false);
    }

    private void subscribe() {
        try (Jedis subscriber = this.createJedisInstance(Configuration.get(MainConfiguration.class).getRedisConnectionConfig())) {
            subscriber.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    switch (channel) {
                        case UPDATE_CHANNEL_NAME -> {
                            VaultUpdateRequest vaultUpdate = gson.fromJson(message, VaultUpdateRequest.class);
                            if (!vaultUpdate.server().equals(serverUUID)) {
                                handleVaultUpdate(vaultUpdate);
                            }
                        }
                        case OPEN_CHANNEL_NAME -> {
                            VaultOpenRequest openRequest = gson.fromJson(message, VaultOpenRequest.class);
                            if (!openRequest.server().equals(serverUUID)) {
                                handleVaultOpen(openRequest);
                            }
                        }
                        case STATE_CHANNEL_NAME -> {
                            VaultStateRequest stateRequest = gson.fromJson(message, VaultStateRequest.class);
                            if(!stateRequest.server().equals(serverUUID)) {
                                handleVaultStateChange(stateRequest);
                            }
                        }
                        case CLOSE_CHANNEL_NAME -> {
                            VaultCloseRequest closeRequest = gson.fromJson(message, VaultCloseRequest.class);
                            if (!closeRequest.server().equals(serverUUID)) {
                                handleVaultCloseRequest(closeRequest);
                            }
                        }
                        case CREATE_CHANNEL_NAME -> {
                            VaultCreateRequest createRequest = gson.fromJson(message, VaultCreateRequest.class);
                            if (!createRequest.server().equals(serverUUID)) {
                                handleVaultCreateRequest(createRequest);
                            }
                        }
                        case SHARE_CHANNEL_NAME -> {
                            VaultShareRequest shareRequest = gson.fromJson(message, VaultShareRequest.class);
                            if (!shareRequest.server().equals(serverUUID)) {
                                handleVaultShareRequest(shareRequest);
                            }
                        }
                        case SIZE_CHANNEL_NAME -> {
                            VaultChangeSizeRequest sizeRequest = gson.fromJson(message, VaultChangeSizeRequest.class);
                            if (!sizeRequest.server().equals(serverUUID)) {
                                handleVaultSizeChange(sizeRequest);
                            }
                        }
                        case ICON_CHANNEL_NAME -> {
                            VaultIconChangeRequest iconRequest = gson.fromJson(message, VaultIconChangeRequest.class);
                            if (!iconRequest.server().equals(serverUUID)) {
                                handleVaultIconChange(iconRequest);
                            }
                        }
                        case DELETE_CHANNEL_NAME -> {
                            VaultDeleteRequest deleteRequest = gson.fromJson(message, VaultDeleteRequest.class);
                            if (!deleteRequest.server().equals(serverUUID)) {
                                handleDeleteRequest(deleteRequest);
                            }
                        }
                    }
                }
            }, UPDATE_CHANNEL_NAME, OPEN_CHANNEL_NAME, STATE_CHANNEL_NAME,
                    CLOSE_CHANNEL_NAME, CREATE_CHANNEL_NAME, SHARE_CHANNEL_NAME,
                    SIZE_CHANNEL_NAME, ICON_CHANNEL_NAME, DELETE_CHANNEL_NAME);
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
