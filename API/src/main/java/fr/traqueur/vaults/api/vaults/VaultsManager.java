package fr.traqueur.vaults.api.vaults;

import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.traqueur.vaults.api.exceptions.IndexOutOfBoundVaultException;
import fr.traqueur.vaults.api.managers.Manager;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VaultsManager extends Manager {

    String VAULT_TABLE_NAME = "vaults";

    Optional<InventoryDefault> getLinkedInventory(UUID uuid);

    void saveVault(Vault vault);

    void linkVaultToInventory(User user, InventoryDefault inventory);

    Vault getOpenedVault(User user);

    void closeVault(User user, Vault vault);

    void openVault(User user, Vault vault);

    void createVault(User creator, VaultOwner owner, int size, int maxVaults, boolean infinite);

    void createVault(UUID vaultId, VaultOwner owner, int size, boolean infinite);

    boolean sizeIsAvailable(int size);

    OwnerResolver getOwnerResolver();

    List<String> getSizeTabulation();

    VaultOwner generateOwner(String type, User receiver);

    List<String> getNumVaultsTabulation();

    Vault getVault(User receiver, int vaultNum) throws IndexOutOfBoundVaultException;

    Vault getVault(UUID vaultId);

    List<Vault> getVaults(User user);

    void handleLeftClick(InventoryClickEvent event, Player player, ItemStack cursor, int slot, Vault vault);

    void handleRightClick(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault);

    void handleShift(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault);

    void handleDrop(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault, boolean b);

    void handleNumberKey(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault);

    void deleteVault(Vault vault, boolean eventLaunch);

    void openVaultChooseMenu(User user, User target);

    User getTargetUser(User user);

    void closeVaultChooseMenu(User user);

    void changeSizeOfVault(User user, Vault vault, int size, Message success, Message transmitted);

    ItemStack cloneItemStack(ItemStack itemStack);

    int addItem(Vault vault, ItemStack item);
}
