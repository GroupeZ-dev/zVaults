package fr.traqueur.vaults.api.config;


public record AnvilMenuConfiguration(String title, String startMessage, String tryAgainMessage) implements Loadable { }
