package fr.traqueur.vaults.distributed;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.MainConfiguration;
import fr.traqueur.vaults.api.distributed.DistributedManager;
import fr.traqueur.vaults.api.distributed.RedisConnectionConfig;
import fr.traqueur.vaults.api.distributed.VaultUpdate;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.distributed.adapter.VaultUpdateAdapter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZDistributedManager implements DistributedManager {

    private final Jedis publisher;
    private final Jedis subscriber;
    private final ExecutorService executorService;
    private final Gson gson;
    private final UUID serverUUID;

    public ZDistributedManager(VaultsPlugin plugin) {
        this.serverUUID = UUID.randomUUID();
        MainConfiguration configuration = Configuration.getConfiguration(MainConfiguration.class);
        RedisConnectionConfig redisConfig = configuration.getRedisConnectionConfig();

        this.executorService = Executors.newSingleThreadExecutor();

        this.publisher = new Jedis(redisConfig.host(), redisConfig.port());
        this.subscriber = new Jedis(redisConfig.host(), redisConfig.port());
        if(redisConfig.password() != null && !redisConfig.password().isEmpty() && (redisConfig.user() == null || redisConfig.user().isEmpty())) {
            this.publisher.auth(redisConfig.password());
            this.subscriber.auth(redisConfig.password());
        } else if(redisConfig.user() != null && !redisConfig.user().isEmpty() && redisConfig.password() != null && !redisConfig.password().isEmpty()) {
            this.publisher.auth(redisConfig.user(), redisConfig.password());
            this.subscriber.auth(redisConfig.user(), redisConfig.password());
        }

        this.gson = new GsonBuilder()
                .registerTypeAdapter(VaultUpdate.class, new VaultUpdateAdapter(plugin.getManager(VaultsManager.class)))
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
    public void publishVaultUpdate(Vault vault, VaultItem item, int slot) {
        VaultUpdate vaultUpdate = new VaultUpdate(serverUUID, vault, item, slot);
        publisher.publish(CHANNEL_NAME, gson.toJson(vaultUpdate, VaultUpdate.class));
    }

    private void handleVaultUpdate(VaultUpdate vaultUpdate) {
        System.out.println("Received vault update");
    }

    private void subscribe() {
        subscriber.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (channel.equals(CHANNEL_NAME)) {
                    VaultUpdate vaultUpdate = gson.fromJson(message, VaultUpdate.class);
                    if(!vaultUpdate.server().equals(serverUUID)) {
                        handleVaultUpdate(vaultUpdate);
                    }
                }
            }
        }, CHANNEL_NAME);
    }

}
