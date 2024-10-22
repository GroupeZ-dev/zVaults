package fr.traqueur.vaults.api.distributed;

import fr.traqueur.vaults.api.vaults.Vault;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record VaultUpdateRequest(UUID server, Vault vault, ItemStack itemStack, int slot) {
}
