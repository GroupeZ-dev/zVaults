package fr.traqueur.vaults.vaults;

import fr.traqueur.vaults.api.data.VaultDTO;
import fr.traqueur.vaults.api.storage.Repository;
import fr.traqueur.vaults.api.vaults.OwnerResolver;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import fr.traqueur.vaults.api.vaults.VaultOwner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ZVaultRepository implements Repository<Vault, VaultDTO> {

    private final OwnerResolver ownerResolver;

    public ZVaultRepository(OwnerResolver ownerResolver) {
        this.ownerResolver = ownerResolver;
    }

    @Override
    public Vault toEntity(VaultDTO vaultDTO) {
        VaultOwner owner = ownerResolver.resolveOwner(vaultDTO.ownerType(), vaultDTO.owner());
        List<VaultItem> content;
        if(vaultDTO.content().isEmpty()) {
            content = new ArrayList<>();
        } else {
            content = Arrays.stream(vaultDTO.content().split(";"))
                    .map(VaultItem::deserialize)
                    .collect(Collectors.toList());
        }
        return new ZVault(vaultDTO.uniqueId(), owner, content, vaultDTO.size(), vaultDTO.infinite());
    }

    @Override
    public VaultDTO toDTO(Vault entity) {
        String serializedContent = entity.getContent().stream().map(VaultItem::serialize).reduce((a, b) -> a + ";" + b).orElse("");
        return new VaultDTO(entity.getUniqueId(), entity.getOwner().getUniqueId(), ownerResolver.getType(entity.getOwner().getClass()),
                            serializedContent,
                            entity.getSize(), entity.isInfinite());
    }
}
