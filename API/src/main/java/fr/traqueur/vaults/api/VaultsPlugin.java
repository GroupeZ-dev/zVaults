package fr.traqueur.vaults.api;

import com.tcoded.folialib.impl.PlatformScheduler;
import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.InventoryManager;
import fr.traqueur.vaults.api.managers.Manager;
import fr.traqueur.vaults.api.storage.Storage;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class VaultsPlugin extends JavaPlugin {

    public abstract PlatformScheduler getScheduler();

    public abstract Storage getStorage();

    public abstract InventoryManager getInventoryManager();

    public abstract ButtonManager getButtonManager();

    public abstract <T extends Manager> T getManager(Class<T> clazz);

}
