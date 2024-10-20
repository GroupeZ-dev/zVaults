package fr.traqueur.vaults.api.vaults;

import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.exceptions.IndexOutOfBoundVaultException;
import fr.traqueur.vaults.api.managers.Manager;
import fr.traqueur.vaults.api.users.User;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface VaultsManager extends Manager {

    String VAULT_TABLE_NAME = "vaults";

    void saveVault(Vault vault);

    void linkVaultToInventory(User user, InventoryDefault inventory);

    Vault getOpenedVault(User user);

    void closeVault(User user, Vault vault);

    void openVault(User user, Vault vault);

    int getAmountFromItem(ItemStack item);

    void createVault(User creator, VaultOwner owner, int size, boolean infinite);

    boolean sizeIsAvailable(int size);

    OwnerResolver getOwnerResolver();

    List<String> getSizeTabulation();

    VaultOwner generateOwner(String type, User receiver);

    List<String> getNumVaultsTabulation();

    Vault getVault(User receiver, int vaultNum) throws IndexOutOfBoundVaultException;

    Vault getVault(UUID vaultId);

    List<Vault> getVaults(User user);

    NamespacedKey getAmountKey();

    void handleLeftClick(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault);

    void handleRightClick(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault);

    void handleShift(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault);

    void handleDrop(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault, boolean b);

    void handleNumberKey(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault);

    void deleteVault(Vault vault);

    void openVaultChooseMenu(User user, User target);

    User getTargetUser(User user);

    void closeVaultChooseMenu(User user);
}
