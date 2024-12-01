package fr.traqueur.vaults.api.config;

import java.util.List;

public record FirstJoinConfig(boolean enabled, List<VaultPreset> vaults) implements Loadable {
}
