package fr.traqueur.vaults.api.config;

public record VaultPreset(int size, boolean infinite) implements Loadable {
}
