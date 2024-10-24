package fr.traqueur.vaults.api.distributed.requests;

import java.util.UUID;

public record VaultCreateRequest(UUID server, UUID vault, String ownerType, UUID owner, int size, boolean infinite){
}
