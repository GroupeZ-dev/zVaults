package fr.traqueur.vaults.api.distributed.requests;

import java.util.UUID;

public record VaultOpenRequest(UUID server, UUID vaultId) {
}
