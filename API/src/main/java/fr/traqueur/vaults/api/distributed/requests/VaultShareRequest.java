package fr.traqueur.vaults.api.distributed.requests;

import fr.traqueur.vaults.api.configurator.ShareType;

import java.util.UUID;

public record VaultShareRequest(UUID server, UUID shared, UUID vault, UUID player, ShareType type) {

}
