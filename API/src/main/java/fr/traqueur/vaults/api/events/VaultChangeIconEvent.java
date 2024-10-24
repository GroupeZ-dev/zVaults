package fr.traqueur.vaults.api.events;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.vaults.Vault;
import org.bukkit.Material;

public class VaultChangeIconEvent extends VaultEvent {

    private final Material material;

    public VaultChangeIconEvent(VaultsPlugin plugin, Vault vault, Material material) {
        super(plugin, vault);
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }
}
