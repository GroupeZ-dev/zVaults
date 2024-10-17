package fr.traqueur.vaults.api.config;

import fr.traqueur.vaults.api.vaults.SizeMode;

public interface VaultsConfiguration extends Configuration {

    SizeMode getSizeMode();

    int getDefaultSize();

    int getMaxVaultsByPlayer();

    boolean isVaultsInfinity();

}
