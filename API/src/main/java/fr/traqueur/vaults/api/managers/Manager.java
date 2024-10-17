package fr.traqueur.vaults.api.managers;

import fr.traqueur.vaults.api.VaultsPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public interface Manager {

    default VaultsPlugin getPlugin() {
        return JavaPlugin.getPlugin(VaultsPlugin.class);
    }

}
