package fr.traqueur.vaults.api.distributed.requests;

import org.bukkit.Material;

import java.util.UUID;

public record VaultIconChangeRequest(UUID server, UUID vault, Material icon) {
}
