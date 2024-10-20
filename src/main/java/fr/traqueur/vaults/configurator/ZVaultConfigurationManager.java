package fr.traqueur.vaults.configurator;

import fr.maxlego08.sarah.MigrationManager;
import fr.traqueur.vaults.api.configurator.SharedAccess;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.data.Saveable;
import fr.traqueur.vaults.api.data.SharedAccessDTO;
import fr.traqueur.vaults.api.storage.Service;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.storage.migrations.SharedAccessMigration;

import java.util.*;

public class ZVaultConfigurationManager implements VaultConfigurationManager, Saveable {

    private final Map<UUID, List<UUID>> openedConfigVaults;
    private final Map<UUID, List<SharedAccess>> sharedAccesses;

    private final Service<SharedAccess, SharedAccessDTO> sharedAccessService;

    public ZVaultConfigurationManager() {
        this.openedConfigVaults = new HashMap<>();
        this.sharedAccesses = new HashMap<>();

        this.sharedAccessService = new Service<>(this.getPlugin(), SharedAccessDTO.class, new ZSharedAccessRepository(this.getPlugin()), SHARED_ACCESS_TABLE);
        MigrationManager.registerMigration(new SharedAccessMigration(SHARED_ACCESS_TABLE));
    }

    @Override
    public void openVaultConfig(User user, Vault vault) {
        this.openedConfigVaults.computeIfAbsent(vault.getUniqueId(), uuid -> new ArrayList<>()).add(user.getUniqueId());
        this.getPlugin().getInventoryManager().openInventory(user.getPlayer(), "vault_config_menu");
    }

    @Override
    public void closeVaultConfig(User user) {
        this.openedConfigVaults.values().forEach(uuids -> uuids.remove(user.getUniqueId()));
    }

    @Override
    public boolean hasAccess(Vault vault, User user) {
        return this.sharedAccesses.get(vault.getUniqueId()).stream().anyMatch(sharedAccess -> sharedAccess.getUser().getUniqueId().equals(user.getUniqueId()));
    }

    @Override
    public void load() {
        this.sharedAccessService.findAll().forEach(sharedAccess -> this.sharedAccesses.computeIfAbsent(sharedAccess.getVault().getUniqueId(), uuid -> new ArrayList<>()).add(sharedAccess));
    }

    @Override
    public void save() {
        this.sharedAccesses.values().forEach(sharedAccesses -> sharedAccesses.forEach(this.sharedAccessService::save));
    }
}
