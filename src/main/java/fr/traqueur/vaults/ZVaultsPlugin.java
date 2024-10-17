package fr.traqueur.vaults;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.InventoryManager;
import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.logging.Logger;
import fr.traqueur.vaults.api.MainConfiguration;
import fr.traqueur.vaults.api.VaultsLogger;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.managers.Manager;
import fr.traqueur.vaults.api.storage.Storage;
import fr.traqueur.vaults.storage.SQLStorage;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public final class ZVaultsPlugin extends VaultsPlugin {

    private PlatformScheduler scheduler;
    private Storage storage;
    private InventoryManager inventoryManager;
    private ButtonManager buttonManager;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        VaultsLogger.info("&e=== ENABLE START ===");
        this.scheduler = new FoliaLib(this).getScheduler();
        this.inventoryManager = this.getProvider(InventoryManager.class);
        this.buttonManager = this.getProvider(ButtonManager.class);

        if(this.inventoryManager == null || this.buttonManager == null) {
            VaultsLogger.severe("InventoryManager or ButtonManager is not found.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        MainConfiguration config = Configuration.registerConfiguration(MainConfiguration.class, new ZMainConfiguration(this));
        config.loadConfig();

        this.storage = new SQLStorage(this, config.getDatabaseConfiguration());

        CommandManager commandManager = new CommandManager(this);
        commandManager.setDebug(config.isDebug());
        commandManager.setLogger(new Logger() {
            @Override
            public void error(String s) {
                VaultsLogger.severe(s);
            }

            @Override
            public void info(String s) {
                VaultsLogger.info(s);
            }
        });

        Configuration.REGISTRY.values().forEach(configuration -> {
            if(!configuration.isLoad()) {
                configuration.loadConfig();
            }
        });

        this.storage.onEnable();


        new ZPlaceholder(this).register();

        VaultsLogger.success("&e=== ENABLE DONE" + " &7(&6" + (System.currentTimeMillis() - start) + "ms&7)&e ===");
    }

    @Override
    public void onDisable() {
        long start = System.currentTimeMillis();
        VaultsLogger.info("&e=== DISABLE START ===");
        this.storage.onDisable();
        VaultsLogger.success("&e=== DISABLE DONE" + " &7(&6" + (System.currentTimeMillis() - start) + "ms&7)&e ===");
    }

    @Override
    public PlatformScheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public Storage getStorage() {
        return this.storage;
    }

    @Override
    public InventoryManager getInventoryManager() {
        return this.inventoryManager;
    }

    @Override
    public ButtonManager getButtonManager() {
        return this.buttonManager;
    }

    @Override
    public <T extends Manager> T getManager(Class<T> clazz) {
        return this.getProvider(clazz);
    }

    private <I extends Manager, T extends I> void registerManager(T instance, Class<I> clazz) {
        this.getServer().getServicesManager().register(clazz, instance, this, ServicePriority.Normal);
        if (Configuration.getConfiguration(MainConfiguration.class).isDebug()) {
            VaultsLogger.info("Registered manager: " + clazz.getSimpleName());
        }
    }

    private <T> T getProvider(Class<T> clazz) {
        RegisteredServiceProvider<T> provider = getServer().getServicesManager().getRegistration(clazz);
        if (provider == null) {
            return null;
        } else {
            return provider.getProvider();
        }
    }
}
