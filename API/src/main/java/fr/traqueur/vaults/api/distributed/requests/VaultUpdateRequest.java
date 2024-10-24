package fr.traqueur.vaults.api.distributed.requests;

import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record VaultUpdateRequest(UUID server, Vault vault, VaultItem itemStack, int slot) {
}
