package fr.traqueur.vaults.api.config;

import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.traqueur.vaults.api.distributed.RedisConnectionConfig;

public interface MainConfiguration extends Configuration {

    DatabaseConfiguration getDatabaseConfiguration();

    RedisConnectionConfig getRedisConnectionConfig();

    boolean isDebug();

    boolean isMultiServerSyncSupport();
}
