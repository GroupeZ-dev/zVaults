package fr.traqueur.vaults.api.configurator;

import fr.traqueur.vaults.api.managers.Manager;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;

import java.util.List;

public interface VaultConfigurationManager extends Manager {

    String SHARED_ACCESS_TABLE = "vaults_shared_access";

    void openVaultConfig(User user, Vault vault);

    void closeVaultConfig(User user);

    boolean hasAccess(Vault vault, User user);

    Vault getOpenedConfig(User user);

    void openInvitationMenu(User user, Vault vault);

    void openAccessManagerMenu(User user, Vault vault);

    void closeAccessManagerMenu(User user);

    Vault getOpenedAccessManager(User user);

    List<User> getWhoCanAccess(Vault vault);

    void removeAccess(User user, Vault vault, User value);
}
