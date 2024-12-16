package fr.traqueur.vaults.converters.impl;

import com.drtshock.playervaults.PlayerVaults;
import com.drtshock.playervaults.vaultmanagement.CardboardBoxSerialization;
import com.drtshock.playervaults.vaultmanagement.VaultManager;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.converters.Converter;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class PlayerVaultXConverter implements Converter {
    @Override
    public void convert() {
        File folder = PlayerVaults.getInstance().getVaultData();
        try (Stream<Path> stream = Files.walk(folder.toPath())) {
            stream.skip(1).map(Path::toFile).filter(File::isFile).filter(e -> e.getName().endsWith(".yml")).forEach(this::loadVaults);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void loadVaults(File file) {
        VaultsManager vaultsManager = this.getManager();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String name = file.getName().replace(".yml", "");
        Set<Integer> numbersVaults = VaultManager.getInstance().getVaultNumbers(name);

        for (int number : numbersVaults) {
            String vaultName = "vault" + number;
            String data = config.getString(vaultName);
            ItemStack[] deserialized = CardboardBoxSerialization.fromStorage(data, name);
            int size = deserialized.length;
            size = Math.min(((size + 8) / 9) * 9, 54);
            Material material = Configuration.get(VaultsConfiguration.class).getVaultIcon();
            vaultsManager.convertVault(UUID.fromString(name), size, false, Arrays.asList(deserialized), material);

        }
    }
}
