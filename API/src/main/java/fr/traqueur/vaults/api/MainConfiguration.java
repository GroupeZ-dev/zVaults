package fr.traqueur.vaults.api;

import fr.maxlego08.sarah.DatabaseConfiguration;
import fr.traqueur.vaults.api.config.Configuration;

public interface MainConfiguration extends Configuration {

    DatabaseConfiguration getDatabaseConfiguration();

    boolean isDebug();

}
