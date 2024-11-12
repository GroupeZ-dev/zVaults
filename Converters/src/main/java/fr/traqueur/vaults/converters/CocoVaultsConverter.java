package fr.traqueur.vaults.converters;

import fr.traqueur.vaults.api.converters.Converter;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import site.dragonstudio.cocovaults.Main;
import site.dragonstudio.cocovaults.config.ConfigLoader;
import site.dragonstudio.cocovaults.util.InventorySerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class CocoVaultsConverter implements Converter {
    @Override
    public void convert() {
        Main main = JavaPlugin.getPlugin(Main.class);
        File folder = new File(main.getDataFolder(), "vaults");
        try (Stream<Path> stream = Files.walk(folder.toPath())) {
            stream.skip(1).map(Path::toFile).filter(File::isFile).filter(e -> e.getName().endsWith(".yml")).forEach(file -> this.loadVaults(main.configLoader, file));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void loadVaults(ConfigLoader configLoader, File file) {
        VaultsManager vaultsManager = this.getManager();
        String uuid = file.getName().replace(".yml", "");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String vaults : config.getConfigurationSection("vaults").getKeys(false)) {
            int i = Integer.parseInt(vaults);
            String serializedInventory = config.getString("vaults." + vaults + ".inventory");
            Inventory deserializedInventory = InventorySerializer.deserializeInventory(serializedInventory, configLoader.getGuiName("Player-Vault").replace("%vault%", String.valueOf(i + 1)));
            List<ItemStack> items = Arrays.asList(deserializedInventory.getContents());
            String serializedIcon = config.getString("vaults." + vaults + ".icon");
            ItemStack icon = InventorySerializer.deserializeItemStack(serializedIcon);
            vaultsManager.convertVault(UUID.fromString(uuid), deserializedInventory.getSize(), false, items, icon.getType());
        }
    }
}
