package fr.traqueur.vaults.converters;

import fr.traqueur.vaults.api.converters.Converter;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;

public enum Converters {

    AXVAULTS("AxVaults", AxVaultsConverter.class),
    PLAYERVAULTX("PlayerVaults", PlayerVaultXConverter.class),
    ENDERVAULTS("EnderVaults", EnderVaultsConverter.class),
    COCOVAULTS("CocoVaults-Lite", CocoVaultsConverter.class),
    ;

    private final String name;
    private final Class<? extends Converter> converter;

    Converters(String name, Class<? extends Converter> converter) {
        this.name = name;
        this.converter = converter;
    }

    public boolean isEnable() {
        return Bukkit.getPluginManager().getPlugin(this.name) != null && Bukkit.getPluginManager().isPluginEnabled(this.name);
    }

    public String getName() {
        return name;
    }

    public void convert() {
        try {
            Converter converter = this.converter.getConstructor().newInstance();
            converter.convert();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
