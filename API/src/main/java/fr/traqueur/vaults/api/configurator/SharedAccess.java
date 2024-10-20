package fr.traqueur.vaults.api.configurator;

import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;

import java.util.UUID;

public interface SharedAccess {

    UUID getUniqueId();

    Vault getVault();

    User getUser();

}
