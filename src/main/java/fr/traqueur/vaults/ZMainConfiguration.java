package fr.traqueur.vaults;

import dev.dejvokep.boostedyaml.YamlDocument;
import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.maxlego08.sarah.database.DatabaseType;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.MainConfiguration;
import fr.traqueur.vaults.api.config.NonLoadable;
import fr.traqueur.vaults.api.distributed.RedisConnectionConfig;

import java.util.List;

public class ZMainConfiguration implements MainConfiguration {

    @NonLoadable
    private final VaultsPlugin plugin;
    @NonLoadable
    private boolean loaded;

    @NonLoadable
    private DatabaseConfiguration databaseConfiguration;
    private boolean debug;
    private boolean multiServerSyncSupport;
    private RedisConnectionConfig redisConfig;
    private List<String> commandAliases;

    public ZMainConfiguration(VaultsPlugin plugin) {
        this.plugin = plugin;
        this.loaded = false;
    }

    @Override
    public String getFile() {
        return "config.yml";
    }

    @Override
    public void loadConfig() {
        YamlDocument config = this.getConfig(this.plugin);

        this.databaseConfiguration = new DatabaseConfiguration(
                config.getString("storage-config.table-prefix"),
                config.getString("storage-config.username"),
                config.getString("storage-config.password"),
                config.getInt("storage-config.port"),
                config.getString("storage-config.host"),
                config.getString("storage-config.database"),
                this.debug,
                DatabaseType.valueOf(config.getString("storage-config.type").toUpperCase())
        );

        this.loaded = true;
    }

    @Override
    public boolean isLoad() {
        return this.loaded;
    }

    @Override
    public DatabaseConfiguration getDatabaseConfiguration() {
        return this.databaseConfiguration;
    }

    @Override
    public RedisConnectionConfig getRedisConnectionConfig() {
        return this.redisConfig;
    }

    @Override
    public boolean isDebug() {
        return this.debug;
    }

    @Override
    public boolean isMultiServerSyncSupport() {
        return multiServerSyncSupport;
    }

    @Override
    public List<String> getAliases() {
        return commandAliases;
    }
}
