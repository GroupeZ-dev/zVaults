package fr.traqueur.vaults.api.distributed;

import fr.traqueur.vaults.api.vaults.Vault;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public record VaultUpdate(UUID server, Vault vault, ItemStack itemStack, int slot) {
}
