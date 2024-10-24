package fr.traqueur.vaults.api.config;

import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.traqueur.vaults.api.distributed.RedisConnectionConfig;

import java.util.List;

public interface MainConfiguration extends Configuration {

    DatabaseConfiguration getDatabaseConfiguration();

    RedisConnectionConfig getRedisConnectionConfig();

    boolean isDebug();

    boolean isMultiServerSyncSupport();

    List<String> getAliases();
}
