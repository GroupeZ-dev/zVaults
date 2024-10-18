package fr.traqueur.vaults.vaults;

import fr.traqueur.vaults.api.data.VaultDTO;
import fr.traqueur.vaults.api.storage.Repository;
import fr.traqueur.vaults.api.vaults.OwnerResolver;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import fr.traqueur.vaults.api.vaults.VaultOwner;

import java.util.Arrays;
import java.util.List;

public class ZVaultRepository implements Repository<Vault, VaultDTO> {

    private final OwnerResolver ownerResolver;

    public ZVaultRepository(OwnerResolver ownerResolver) {
        this.ownerResolver = ownerResolver;
    }

    @Override
    public Vault toEntity(VaultDTO vaultDTO) {
        VaultOwner owner = ownerResolver.resolveOwner(vaultDTO.ownerType(), vaultDTO.owner());
        List<VaultItem> content = Arrays.stream(vaultDTO.content().split(";")).map(VaultItem::deserialize).toList();
        return new ZVault(vaultDTO.uniqueId(), owner, content, vaultDTO.size(), vaultDTO.infinite());
    }

    @Override
    public VaultDTO toDTO(Vault entity) {
        return new VaultDTO(entity.getUniqueId(), entity.getOwner().getUniqueId(), ownerResolver.getType(entity.getOwner().getClass()),
                            entity.getContent().stream().map(VaultItem::serialize).reduce((a, b) -> a + ";" + b).orElse(""),
                            entity.getSize(), entity.isInfinite());
    }
}
