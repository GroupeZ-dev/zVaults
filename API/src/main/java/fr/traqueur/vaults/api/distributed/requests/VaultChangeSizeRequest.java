package fr.traqueur.vaults.api.distributed.requests;

import java.util.UUID;

public record VaultChangeSizeRequest(UUID server, UUID vault, int size) {
}
