package fr.traqueur.vaults.api.converters;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.plugin.java.JavaPlugin;

public interface Converter {

    void convert();

    default VaultsManager getManager() {
        return JavaPlugin.getPlugin(VaultsPlugin.class).getManager(VaultsManager.class);
    }

}
