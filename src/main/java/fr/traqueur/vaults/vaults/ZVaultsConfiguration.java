package fr.traqueur.vaults.vaults;

import fr.traqueur.vaults.api.config.NonLoadable;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.vaults.SizeMode;

public class ZVaultsConfiguration implements VaultsConfiguration {

    @NonLoadable
    private boolean load;

    private int defaultSize;
    private int maxVaultsPerPlayer;
    private boolean infiniteVaults;
    private SizeMode sizeMode;

    public ZVaultsConfiguration() {
        this.load = false;
    }

    @Override
    public String getFile() {
        return "vaults.yml";
    }

    @Override
    public void loadConfig() {
        this.load = true;
    }

    @Override
    public boolean isLoad() {
        return load;
    }

    @Override
    public SizeMode getSizeMode() {
        return sizeMode;
    }

    @Override
    public int getDefaultSize() {
        return defaultSize;
    }

    @Override
    public int getMaxVaultsByPlayer() {
        return maxVaultsPerPlayer;
    }

    @Override
    public boolean isVaultsInfinity() {
        return infiniteVaults;
    }
}
