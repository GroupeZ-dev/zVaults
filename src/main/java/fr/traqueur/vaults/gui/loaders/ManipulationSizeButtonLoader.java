package fr.traqueur.vaults.gui.loaders;

import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import fr.maxlego08.menu.api.loader.ButtonLoader;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.gui.buttons.sizes.ManipulationSizeButton;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;

public class ManipulationSizeButtonLoader implements ButtonLoader {

    private final VaultsPlugin plugin;
    private final Class<? extends ManipulationSizeButton> btnClazz;
    private final String name;

    public ManipulationSizeButtonLoader(VaultsPlugin plugin, Class<? extends ManipulationSizeButton> btnClazz, String name) {
        this.plugin = plugin;
        this.btnClazz = btnClazz;
        this.name = name;
    }

    @Override
    public Class<? extends Button> getButton() {
        return this.btnClazz;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public Button load(YamlConfiguration yamlConfiguration, String path, DefaultButtonValue defaultButtonValue) {

        int size = yamlConfiguration.getInt(path + "size");
        try {
            return this.btnClazz.getConstructor(VaultsPlugin.class, int.class).newInstance(this.plugin, size);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
