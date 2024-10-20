package fr.traqueur.vaults.api.configurator;

import fr.traqueur.vaults.api.managers.Manager;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;

public interface VaultConfigurationManager extends Manager {

    String SHARED_ACCESS_TABLE = "vaults_shared_access";

    void openVaultConfig(User user, Vault vault);

    void closeVaultConfig(User user);

    boolean hasAccess(Vault vault, User user);
}
