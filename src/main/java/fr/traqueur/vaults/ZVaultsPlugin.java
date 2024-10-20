package fr.traqueur.vaults;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.button.loader.NoneLoader;
import fr.maxlego08.menu.exceptions.InventoryException;
import fr.traqueur.commands.api.CommandManager;
import fr.traqueur.commands.api.logging.Logger;
import fr.traqueur.vaults.api.VaultsLogger;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.commands.CommandsHandler;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.LangConfiguration;
import fr.traqueur.vaults.api.config.MainConfiguration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.data.Saveable;
import fr.traqueur.vaults.api.managers.Manager;
import fr.traqueur.vaults.api.messages.MessageResolver;
import fr.traqueur.vaults.api.storage.Storage;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.commands.VaultsCommand;
import fr.traqueur.vaults.commands.arguments.OwnerTypeArgument;
import fr.traqueur.vaults.commands.arguments.UserArgument;
import fr.traqueur.vaults.configurator.ZVaultConfigurationManager;
import fr.traqueur.vaults.gui.VaultAccessManagerMenu;
import fr.traqueur.vaults.gui.VaultConfigMenu;
import fr.traqueur.vaults.gui.VaultMenu;
import fr.traqueur.vaults.gui.VaultsChooseMenu;
import fr.traqueur.vaults.gui.buttons.*;
import fr.traqueur.vaults.lang.ZLangConfiguration;
import fr.traqueur.vaults.storage.SQLStorage;
import fr.traqueur.vaults.users.ZUserManager;
import fr.traqueur.vaults.vaults.ZVaultsConfiguration;
import fr.traqueur.vaults.vaults.ZVaultsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class ZVaultsPlugin extends VaultsPlugin {

    private PlatformScheduler scheduler;
    private Storage storage;
    private InventoryManager inventoryManager;
    private ButtonManager buttonManager;
    private MessageResolver messageResolver;
    private List<Saveable> saveables;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        VaultsLogger.info("&e=== ENABLE START ===");
        this.saveables = new ArrayList<>();
        this.scheduler = new FoliaLib(this).getScheduler();
        this.inventoryManager = this.getProvider(InventoryManager.class);
        this.buttonManager = this.getProvider(ButtonManager.class);

        if(this.inventoryManager == null || this.buttonManager == null) {
            VaultsLogger.severe("InventoryManager or ButtonManager is not found.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.messageResolver = new MessageResolver(this);

        LangConfiguration langConfiguration = Configuration.registerConfiguration(LangConfiguration.class, new ZLangConfiguration(this));
        MainConfiguration config = Configuration.registerConfiguration(MainConfiguration.class, new ZMainConfiguration(this));
        config.load();
        langConfiguration.load();

        this.storage = new SQLStorage(this, config.getDatabaseConfiguration());

        CommandManager commandManager = new CommandManager(this);
        commandManager.setDebug(config.isDebug());
        commandManager.setMessageHandler(new CommandsHandler());
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

        var vaultConfig = Configuration.registerConfiguration(VaultsConfiguration.class, new ZVaultsConfiguration());

        Configuration.REGISTRY.values().forEach(configuration -> {
            if(!configuration.isLoad()) {
                configuration.load();
            }
        });

        UserManager userManager = this.registerManager(new ZUserManager(), UserManager.class);
        VaultsManager vaultsManager = this.registerManager(new ZVaultsManager(vaultConfig), VaultsManager.class);
        this.registerManager(new ZVaultConfigurationManager(), VaultConfigurationManager.class);

        buttonManager.unregisters(this);
        buttonManager.register(new NoneLoader(this, VaultButton.class, "zvaults_vaults"));
        buttonManager.register(new NoneLoader(this, VaultItemButton.class, "zvaults_vault_items"));
        buttonManager.register(new NoneLoader(this, VaultInviteButton.class, "zvaults_invite_player"));
        buttonManager.register(new NoneLoader(this, UserAccessButton.class, "zvaults_vault_users_access"));
        buttonManager.register(new NoneLoader(this, CustomizeIconButton.class, "zvaults_customize_icon"));

        inventoryManager.deleteInventories(this);
        try {
            this.inventoryManager.loadInventoryOrSaveResource(this, "inventories/vaults_choose_menu.yml", VaultsChooseMenu.class);
            this.inventoryManager.loadInventoryOrSaveResource(this, "inventories/vault_menu.yml", VaultMenu.class);
            this.inventoryManager.loadInventoryOrSaveResource(this, "inventories/vault_config_menu.yml", VaultConfigMenu.class);
            this.inventoryManager.loadInventoryOrSaveResource(this, "inventories/vault_access_manager_menu.yml", VaultAccessManagerMenu.class);
        } catch (InventoryException e) {
            throw new RuntimeException(e);
        }

        commandManager.registerConverter(String.class, "ownerType", new OwnerTypeArgument(vaultsManager.getOwnerResolver()));
        commandManager.registerConverter(User.class, "user", new UserArgument(userManager));

        commandManager.registerCommand(new VaultsCommand(this));

        this.storage.onEnable();

        this.saveables.forEach(Saveable::load);

        this.scheduler.runTimerAsync(() -> this.saveables.forEach(Saveable::save), 1, 1, TimeUnit.HOURS);

        if(this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ZPlaceholder(this).register();
        }

        Bukkit.getOnlinePlayers().forEach(userManager::handleJoin);

        VaultsLogger.success("&e=== ENABLE DONE" + " &7(&6" + (System.currentTimeMillis() - start) + "ms&7)&e ===");
    }

    @Override
    public void onDisable() {
        long start = System.currentTimeMillis();
        VaultsLogger.info("&e=== DISABLE START ===");
        Bukkit.getOnlinePlayers().forEach(Player::closeInventory);
        if(this.messageResolver != null) {
            this.messageResolver.close();
        }
        if(this.scheduler != null) {
            this.scheduler.cancelAllTasks();
        }
        if(this.saveables != null) {
            this.saveables.forEach(Saveable::save);
        }
        if(this.storage != null) {
            this.storage.onDisable();
        }
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

    @Override
    public MessageResolver getMessageResolver() {
        return this.messageResolver;
    }

    private <I extends Manager, T extends I> I registerManager(T instance, Class<I> clazz) {
        this.getServer().getServicesManager().register(clazz, instance, this, ServicePriority.Normal);
        if (Configuration.getConfiguration(MainConfiguration.class).isDebug()) {
            VaultsLogger.info("Registered manager: " + clazz.getSimpleName());
        }
        if(instance instanceof Saveable saveable) {
            this.saveables.add(saveable);
        }
        return instance;
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
