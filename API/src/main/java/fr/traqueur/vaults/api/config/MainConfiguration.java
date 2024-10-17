package fr.traqueur.vaults.api.config;

import fr.maxlego08.sarah.DatabaseConfiguration;

public interface MainConfiguration extends Configuration {

    DatabaseConfiguration getDatabaseConfiguration();

    boolean isDebug();

}
