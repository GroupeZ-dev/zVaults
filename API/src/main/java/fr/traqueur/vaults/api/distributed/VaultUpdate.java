package fr.traqueur.vaults.api.distributed;

import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;

import java.util.UUID;

public record VaultUpdate(UUID server, Vault vault, VaultItem vaultItem, int slot) {
}
