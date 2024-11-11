package fr.traqueur.vaults.converters;

import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import fr.traqueur.vaults.api.converters.Converter;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AxVaultsConverter implements Converter {
    @Override
    public void convert() {

        VaultsManager vaultsManager = this.getPlugin().getManager(VaultsManager.class);

        for (Vault vault : VaultManager.getVaults().stream()
                .filter(v -> Arrays.stream(v.getStorage().getContents()).anyMatch(Objects::nonNull)).toList()) {
            Material icon = vault.getIcon();
            UUID owner = vault.getUUID();
            int size = vault.getStorage().getSize();
            List<ItemStack> content = Arrays.asList(vault.getStorage().getContents());
            vaultsManager.convertVault(owner, size, false, content, icon);
        }

    }
}
