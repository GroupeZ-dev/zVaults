package fr.traqueur.vaults.api.distributed.requests;

import java.util.UUID;

public record VaultDeleteRequest(UUID server, UUID vaultId) {
}
