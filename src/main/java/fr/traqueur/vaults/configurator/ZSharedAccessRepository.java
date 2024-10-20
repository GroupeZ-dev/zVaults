package fr.traqueur.vaults.configurator;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.configurator.SharedAccess;
import fr.traqueur.vaults.api.data.SharedAccessDTO;
import fr.traqueur.vaults.api.storage.Repository;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;

public class ZSharedAccessRepository implements Repository<SharedAccess, SharedAccessDTO> {

    private final UserManager userManager;
    private final VaultsManager vaultsManager;

    public ZSharedAccessRepository(VaultsPlugin plugin) {
        this.userManager = plugin.getManager(UserManager.class);
        this.vaultsManager = plugin.getManager(VaultsManager.class);
    }

    @Override
    public SharedAccess toEntity(SharedAccessDTO sharedAccessDTO) {
        Vault vault = this.vaultsManager.getVault(sharedAccessDTO.vaultId());
        User user = this.userManager.getUser(sharedAccessDTO.userId()).orElseThrow();
        return new ZSharedAccess(sharedAccessDTO.uuid(), vault, user);
    }

    @Override
    public SharedAccessDTO toDTO(SharedAccess entity) {
        return new SharedAccessDTO(entity.getUniqueId(), entity.getVault().getUniqueId(), entity.getUser().getUniqueId());
    }
}
