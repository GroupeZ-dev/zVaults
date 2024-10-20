package fr.traqueur.vaults.vaults;

import fr.maxlego08.menu.MenuItemStack;
import fr.maxlego08.menu.exceptions.InventoryException;
import fr.maxlego08.menu.loader.MenuItemStackLoader;
import fr.maxlego08.menu.zcore.utils.loader.Loader;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.InvitePlayerMenuConfiguration;
import fr.traqueur.vaults.api.config.NonLoadable;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.vaults.SizeMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ZVaultsConfiguration implements VaultsConfiguration {

    @NonLoadable
    private boolean load;

    private int defaultSize;
    private int maxVaultsPerPlayer;
    private boolean infiniteVaults;
    private SizeMode sizeMode;
    private InvitePlayerMenuConfiguration invitePlayerMenu;
    @NonLoadable
    private final Map<String, MenuItemStack> vaultsIcons;
    @NonLoadable
    private final Map<String, String> vautlsTitle;

    public ZVaultsConfiguration() {
        this.vaultsIcons = new HashMap<>();
        this.vautlsTitle = new HashMap<>();
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
        config.getConfigurationSection("vaults_icons").getKeys(false).forEach(key -> {
            try {
                this.vaultsIcons.put(key, loader.load(config, "vaults_icons." + key+ ".", file));
            } catch (InventoryException e) {
                throw new RuntimeException(e);
            }
        });

        config.getConfigurationSection("vault_title").getKeys(false).forEach(key -> {
            this.vautlsTitle.put(key, config.getString("vault_title." + key));
        });
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
    public int getMaxVaultsByPlayer() {
        return maxVaultsPerPlayer;
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
}
