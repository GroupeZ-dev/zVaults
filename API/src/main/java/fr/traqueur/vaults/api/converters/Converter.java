package fr.traqueur.vaults.api.converters;

import fr.traqueur.vaults.api.VaultsPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public interface Converter {

    void convert();

    default VaultsPlugin getPlugin() {
        return JavaPlugin.getPlugin(VaultsPlugin.class);
    }

}
