package fr.traqueur.vaults.converters;

import com.github.dig.endervaults.api.EnderVaultsPlugin;
import com.github.dig.endervaults.api.VaultPluginProvider;
import com.github.dig.endervaults.api.vault.Vault;
import com.github.dig.endervaults.api.vault.VaultPersister;
import com.github.dig.endervaults.api.vault.VaultRegistry;
import com.github.dig.endervaults.api.vault.metadata.VaultDefaultMetadata;
import com.github.dig.endervaults.bukkit.vault.BukkitVault;
import fr.traqueur.vaults.api.converters.Converter;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EnderVaultsConverter implements Converter {
    @Override
    public void convert() {
        VaultsManager manager = this.getManager();
        EnderVaultsPlugin plugin = VaultPluginProvider.getPlugin();
        VaultRegistry registry = plugin.getRegistry();

        for (UUID owner : registry.getAllOwners()) {
            for (BukkitVault vault : registry.get(owner).values().stream().map(v -> (BukkitVault) v).toList()) {
                int size = vault.getSize();
                Material material = Material.valueOf(vault.getMetadata().get(VaultDefaultMetadata.ICON.getKey()).toString());
                List<ItemStack> items = Arrays.asList(vault.getInventory().getContents());
                manager.convertVault(owner, size, false, items, material);
            }
        }
    }
}
