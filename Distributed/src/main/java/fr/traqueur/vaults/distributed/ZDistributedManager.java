package fr.traqueur.vaults.distributed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.traqueur.vaults.api.VaultsLogger;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.MainConfiguration;
import fr.traqueur.vaults.api.distributed.DistributedManager;
import fr.traqueur.vaults.api.distributed.RedisConnectionConfig;
import fr.traqueur.vaults.api.distributed.VaultUpdateRequest;
import fr.traqueur.vaults.api.distributed.requests.VaultOpenAckRequest;
import fr.traqueur.vaults.api.distributed.requests.VaultOpenRequest;
import fr.traqueur.vaults.api.events.VaultOpenEvent;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.distributed.adapter.VaultUpdateAdapter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import javax.print.attribute.standard.PresentationDirection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class ZDistributedManager implements DistributedManager {

    private final Jedis publisher;
    private final Jedis subscriber;
    private final ExecutorService executorService;
    private final Gson gson;
    private final UUID serverUUID;
    private final VaultsManager vaultsManager;


    public ZDistributedManager(VaultsPlugin plugin) {
        this.serverUUID = UUID.randomUUID();
        MainConfiguration configuration = Configuration.getConfiguration(MainConfiguration.class);
        RedisConnectionConfig redisConfig = configuration.getRedisConnectionConfig();

        this.executorService = Executors.newSingleThreadExecutor();

        this.publisher = this.createJedisInstance(redisConfig);
        this.subscriber = this.createJedisInstance(redisConfig);

        this.vaultsManager = plugin.getManager(VaultsManager.class);

        this.gson = new GsonBuilder()
                .registerTypeAdapter(VaultUpdateRequest.class, new VaultUpdateAdapter(plugin.getManager(VaultsManager.class)))
                .create();

        this.executorService.submit(this::subscribe);
        plugin.getServer().getPluginManager().registerEvents(new ZDistributedListener(this), plugin);
    }

    @Override
    public void disable() {
        this.executorService.shutdownNow();
        this.publisher.close();
        this.subscriber.close();
    }

    @Override
    public void publishVaultUpdate(Vault vault, ItemStack item, int slot) {
        VaultUpdateRequest vaultUpdate = new VaultUpdateRequest(serverUUID, vault, item, slot);
        publisher.publish(UPDATE_CHANNEL_NAME, gson.toJson(vaultUpdate, VaultUpdateRequest.class));
    }

    @Override
    public void publishOpenRequest(VaultOpenEvent event) {
        if(Configuration.getConfiguration(MainConfiguration.class).isDebug()) {
            VaultsLogger.info("Sending open request for vault " + event.getVault().getUniqueId());
        }
        VaultOpenRequest openRequest = new VaultOpenRequest(serverUUID, event.getVault().getUniqueId());
        publisher.publish(OPEN_CHANNEL_NAME, gson.toJson(openRequest, VaultOpenRequest.class));
        this.waitForVaultSaveConfirmation(event);
    }

    public void waitForVaultSaveConfirmation(VaultOpenEvent event) {

        try (Jedis openAckSubscriber = this.createJedisInstance(Configuration.getConfiguration(MainConfiguration.class).getRedisConnectionConfig())) {
            Future<?> future = executorService.submit(() -> {
                openAckSubscriber.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        VaultOpenAckRequest request = gson.fromJson(message, VaultOpenAckRequest.class);
                        if (request.server().equals(serverUUID) && event.getVault().getUniqueId().equals(request.vault())) {
                            if(Configuration.getConfiguration(MainConfiguration.class).isDebug()) {
                                VaultsLogger.info("Received open ack for vault " + request.vault());
                            }
                            event.setContent(request.items());
                            openAckSubscriber.close();
                        }
                    }
                }, OPEN_ACK_CHANNEL_NAME);
            });

            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (Exception ignored) {}
        }
    }

    private void handleVaultUpdate(VaultUpdateRequest vaultUpdate) {
        this.vaultsManager.getLinkedInventory(vaultUpdate.vault().getUniqueId()).ifPresent(inventory -> {
            if (Configuration.getConfiguration(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Received vault update for vault " + vaultUpdate.vault().getUniqueId() + " on slot " + vaultUpdate.slot());
            }
            inventory.getSpigotInventory().setItem(vaultUpdate.slot(), vaultUpdate.itemStack());
        });
    }

    private void handleVaultOpen(VaultOpenRequest openRequest) {
        this.vaultsManager.getLinkedInventory(openRequest.vaultId()).ifPresent(inventory -> {
            if (Configuration.getConfiguration(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Received open request for vault " + openRequest.vaultId());
            }
            Vault vault = this.vaultsManager.getVault(openRequest.vaultId());

            List<VaultItem> items = new ArrayList<>();

            for (int i = 0; i < vault.getSize(); i++) {
                ItemStack item = inventory.getSpigotInventory().getItem(i);
                VaultItem vaultItem;
                if (item != null) {
                    vaultItem = new VaultItem(item, this.vaultsManager.getAmountFromItem(item));
                } else {
                    vaultItem = new VaultItem(new ItemStack(Material.AIR), 1);
                }
                items.add(vaultItem);
            }
            var request = new VaultOpenAckRequest(openRequest.server(), openRequest.vaultId(), items);
            if (Configuration.getConfiguration(MainConfiguration.class).isDebug()) {
                VaultsLogger.info("Sending open ack for vault " + openRequest.vaultId());
            }
            publisher.publish(OPEN_ACK_CHANNEL_NAME, gson.toJson(request, VaultOpenAckRequest.class));
        });
    }


    private void subscribe() {
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
