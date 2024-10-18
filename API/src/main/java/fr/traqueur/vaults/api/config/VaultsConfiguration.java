package fr.traqueur.vaults.api.config;

import fr.maxlego08.menu.MenuItemStack;
import fr.traqueur.vaults.api.vaults.SizeMode;

public interface VaultsConfiguration extends Configuration {

    SizeMode getSizeMode();

    int getDefaultSize();

    int getMaxVaultsByPlayer();

    boolean isVaultsInfinity();

    MenuItemStack getIcon(String id);

    String getVaultTitle();
}
