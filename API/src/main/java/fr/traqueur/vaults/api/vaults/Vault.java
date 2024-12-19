package fr.traqueur.vaults.api.vaults;

import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.users.User;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public interface Vault {

    UUID getUniqueId();

    long getId();

    void setId(long id);

    VaultOwner getOwner();

    String getName();

    void setName(String name);

    int getSize();

    void setSize(int size);

    List<VaultItem> getContent();

    void setContent(List<VaultItem> content);

    boolean isInfinite();

    Material getIcon();

    void setIcon(Material icon);

    VaultItem getInSlot(int slot);

    boolean isAutoPickup();

    default boolean isOwner(User user) {
        return this.getOwner().hasAccess(user.getUniqueId());
    }

    default boolean hasAccess(User receiver) {
        VaultsPlugin plugin = JavaPlugin.getPlugin(VaultsPlugin.class);
        ;
        return this.getOwner().hasAccess(receiver.getUniqueId()) || plugin.getManager(VaultConfigurationManager.class).hasAccess(this, receiver);
    }

    void setAutoPickup(boolean autoPickup);

    void setMaxStackSize(int maxStackSize);

    int getMaxStackSize();
}
