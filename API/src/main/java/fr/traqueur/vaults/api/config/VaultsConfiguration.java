package fr.traqueur.vaults.api.config;

import fr.maxlego08.menu.MenuItemStack;
import fr.traqueur.vaults.api.vaults.SizeMode;
import org.bukkit.Material;

public interface VaultsConfiguration extends Configuration {

    SizeMode getSizeMode();

    int getDefaultSize();

    boolean isVaultsInfinity();

    MenuItemStack getIcon(String id);

    String getVaultTitle(String key);

    InvitePlayerMenuConfiguration getInvitePlayerMenuConfiguration();

    Material getVaultIcon();

    int getMaxVaultsByOwnerType(String ownerType);

    boolean isCloseVaultOpenChooseMenu();

    String getAutoPickupValue(boolean autoPickup);
}
