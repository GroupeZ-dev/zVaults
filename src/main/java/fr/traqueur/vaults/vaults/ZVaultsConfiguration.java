package fr.traqueur.vaults.vaults;

import fr.maxlego08.menu.MenuItemStack;
import fr.maxlego08.menu.exceptions.InventoryException;
import fr.maxlego08.menu.loader.MenuItemStackLoader;
import fr.maxlego08.menu.zcore.utils.loader.Loader;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.*;
import fr.traqueur.vaults.api.vaults.SizeMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ZVaultsConfiguration implements VaultsConfiguration {

    @NonLoadable
    private boolean load;

    private int defaultSize;
    private boolean infiniteVaults;
    private SizeMode sizeMode;
    private InvitePlayerMenuConfiguration invitePlayerMenu;
    @NonLoadable
    private final Map<String, MenuItemStack> vaultsIcons;
    @NonLoadable
    private final Map<String, String> vautlsTitle;
    private Material openVaultDefaultMaterial;
    private final Map<String, Integer> maxVaultsByOwnerType;
    private boolean closeVaultOpenChooseMenu;
    private AutoPickupConfig autopickupValues;
    private int stackSizeInfiniteVaults;
    @NonLoadable
    private FirstJoinConfig firstJoinGiveVault;

    public ZVaultsConfiguration() {
        this.vaultsIcons = new HashMap<>();
        this.vautlsTitle = new HashMap<>();
        this.maxVaultsByOwnerType = new HashMap<>();
        this.load = false;
    }

    @Override
    public String getFile() {
        return "vaults.yml";
    }

    @Override
    public void loadConfig() {
        VaultsPlugin plugin = JavaPlugin.getPlugin(VaultsPlugin.class);
        var file = new File(plugin.getDataFolder(), this.getFile());
        var config = YamlConfiguration.loadConfiguration(file);
        Loader<MenuItemStack> loader = new MenuItemStackLoader(plugin.getInventoryManager());
        config.getConfigurationSection("vaults-icons").getKeys(false).forEach(key -> {
            try {
                this.vaultsIcons.put(key, loader.load(config, "vaults-icons." + key+ ".", file));
            } catch (InventoryException e) {
                throw new RuntimeException(e);
            }
        });

        config.getConfigurationSection("vault-title").getKeys(false).forEach(key -> {
            this.vautlsTitle.put(key, config.getString("vault-title." + key));
        });
        config.getConfigurationSection("max-vaults").getKeys(false).forEach(key -> {
            this.maxVaultsByOwnerType.put(key, config.getInt("max-vaults." + key));
        });

        boolean enabled = config.getBoolean("first-join-give-vault.enabled");

        var presets = config.getMapList("first-join-give-vault.vaults").stream().map(map -> {
            boolean infinite = Boolean.parseBoolean(map.get("infinite").toString());
            int size = Integer.parseInt(map.get("size").toString());
            return new VaultPreset(size, infinite);
        }).collect(Collectors.toList());

        this.firstJoinGiveVault = new FirstJoinConfig(enabled, presets);

        this.load = true;
    }

    @Override
    public boolean isLoad() {
        return load;
    }

    @Override
    public SizeMode getSizeMode() {
        return sizeMode;
    }

    @Override
    public int getDefaultSize() {
        return defaultSize;
    }

    @Override
    public boolean isVaultsInfinity() {
        return infiniteVaults;
    }

    @Override
    public MenuItemStack getIcon(String id) {
        return this.vaultsIcons.getOrDefault(id, null);
    }

    @Override
    public String getVaultTitle(String key) {
        return this.vautlsTitle.getOrDefault(key, this.vautlsTitle.get("default"));
    }

    @Override
    public InvitePlayerMenuConfiguration getInvitePlayerMenuConfiguration() {
        return this.invitePlayerMenu;
    }

    @Override
    public Material getVaultIcon() {
        return this.openVaultDefaultMaterial;
    }

    @Override
    public int getMaxVaultsByOwnerType(String ownerType) {
        return this.maxVaultsByOwnerType.getOrDefault(ownerType, -1);
    }

    @Override
    public boolean isCloseVaultOpenChooseMenu() {
        return closeVaultOpenChooseMenu;
    }

    @Override
    public String getAutoPickupValue(boolean autoPickup) {
        return autoPickup ? autopickupValues.trueValue() : autopickupValues.falseValue();
    }

    @Override
    public int getStackSizeInfiniteVaults() {
        return stackSizeInfiniteVaults;
    }

    @Override
    public FirstJoinConfig getFirstJoinGiveVault() {
        return firstJoinGiveVault;
    }
}
