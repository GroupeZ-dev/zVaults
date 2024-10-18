package fr.traqueur.vaults.vaults;

import fr.traqueur.vaults.api.data.Saveable;
import fr.traqueur.vaults.api.data.VaultDTO;
import fr.traqueur.vaults.api.storage.Service;
import fr.traqueur.vaults.api.vaults.OwnerResolver;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ZVaultsManager implements VaultsManager, Saveable {

    private final OwnerResolver ownerResolver;
    private final Service<Vault, VaultDTO> vaultService;
    private final Map<UUID, List<Vault>> vaults;

    public ZVaultsManager() {
        this.ownerResolver = new OwnerResolver();
        this.registerResolvers(this.ownerResolver);

        this.vaults = new HashMap<>();

        this.vaultService = new Service<>(this.getPlugin(), VaultDTO.class, new ZVaultRepository(this.ownerResolver), VAULT_TABLE_NAME);
    }

    @Override
    public void save() {
        this.vaults.values().stream().flatMap(List::stream).forEach(this.vaultService::save);
    }


    private void registerResolvers(OwnerResolver ownerResolver) {
        ownerResolver.registerOwnerType("player", ZPlayerOwner.class);
    }

}
