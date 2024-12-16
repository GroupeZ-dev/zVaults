package fr.traqueur.vaults.api;

import org.bukkit.Bukkit;

public enum Plugins {

    PAPI("PlaceholderAPI"),
    SUPERIOR("SuperiorSkyblock2")
    ;

    private final String name;

    Plugins(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isEnable() {
        return Bukkit.getServer().getPluginManager().getPlugin(name) != null;
    }
}
