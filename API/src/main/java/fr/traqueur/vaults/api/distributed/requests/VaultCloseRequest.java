package fr.traqueur.vaults.api.distributed.requests;

import fr.traqueur.vaults.api.vaults.VaultItem;

import java.util.List;
import java.util.UUID;

public record VaultCloseRequest(UUID server, UUID vault, List<VaultItem> content) {
}
