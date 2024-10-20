package fr.traqueur.vaults.vaults;

import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.sarah.MigrationManager;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.data.Saveable;
import fr.traqueur.vaults.api.data.VaultDTO;
import fr.traqueur.vaults.api.exceptions.IndexOutOfBoundVaultException;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.storage.Service;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.*;
import fr.traqueur.vaults.storage.migrations.VaultsMigration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ZVaultsManager implements VaultsManager, Saveable {

    private final VaultsConfiguration configuration;
    private final OwnerResolver ownerResolver;
    private final Service<Vault, VaultDTO> vaultService;
    private final Map<UUID, Vault> vaults;
    private final Map<UUID, List<UUID>> openedVaults;
    private final Map<UUID, InventoryDefault> linkedVaultToInventory;

    public ZVaultsManager(VaultsConfiguration configuration) {
        this.configuration = configuration;
        this.ownerResolver = new OwnerResolver();
        this.registerResolvers(this.ownerResolver);

        this.vaults = new HashMap<>();
        this.openedVaults = new HashMap<>();
        this.linkedVaultToInventory = new HashMap<>();

        this.vaultService = new Service<>(this.getPlugin(), VaultDTO.class, new ZVaultRepository(this.ownerResolver), VAULT_TABLE_NAME);
        MigrationManager.registerMigration(new VaultsMigration(VAULT_TABLE_NAME));
    }

    @Override
    public void saveVault(Vault vault) {
        this.vaultService.save(vault);
    }

    @Override
    public void openVault(User user, Vault vault) {
        this.openedVaults.computeIfAbsent(vault.getUniqueId(), uuid -> new ArrayList<>()).add(user.getUniqueId());
        if(this.linkedVaultToInventory.containsKey(vault.getUniqueId())) {
            user.getPlayer().openInventory(this.linkedVaultToInventory.get(vault.getUniqueId()).getSpigotInventory());
            return;
        }
        this.getPlugin().getInventoryManager().openInventory(user.getPlayer(), "vault_menu");
    }

    @Override
    public void linkVaultToInventory(User user, InventoryDefault inventory) {
        Vault vault = this.getOpenedVault(user);
        this.linkedVaultToInventory.put(vault.getUniqueId(), inventory);
    }

    @Override
    public Vault getOpenedVault(User user) {
        return this.vaults.values()
                .stream()
                .filter(vault -> this.openedVaults.getOrDefault(vault.getUniqueId(), Collections.emptyList()).contains(user.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void closeVault(User user, Vault vault) {
        this.openedVaults.computeIfAbsent(vault.getUniqueId(), k -> new ArrayList<>()).remove(user.getUniqueId());
        if(this.openedVaults.getOrDefault(vault.getUniqueId(), Collections.emptyList()).isEmpty()) {
            this.linkedVaultToInventory.remove(vault.getUniqueId());
            this.saveVault(vault);
        }
    }

    @Override
    public int getAmountFromItem(ItemStack item) {
        if(item == null || item.getType().isAir()) {
            return 1;
        }
        ItemMeta meta = item.getItemMeta();
        if(meta == null) {
            return item.getAmount();
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(this.getAmountKey(), PersistentDataType.INTEGER, item.getAmount());
    }

    @Override
    public void createVault(User creator, VaultOwner owner, int size, boolean infinite) {
        var vaults = this.getVaults(owner.getUniqueId());
        if(this.configuration.getMaxVaultsByPlayer() != -1 && vaults.size() >= this.configuration.getMaxVaultsByPlayer()) {
            creator.sendMessage(Message.MAX_VAULTS_REACHED);
            return;
        }
        Vault vault = new ZVault(owner, size, infinite);
        vaults.add(vault);
        this.vaultService.save(vault);
        this.vaults.put(vault.getUniqueId(), vault);
        creator.sendMessage(Message.VAULT_CREATED);
        owner.sendMessage(Message.RECEIVE_NEW_VAULT);
    }

    @Override
    public boolean sizeIsAvailable(int size) {
        return switch (configuration.getSizeMode()) {
            case DEFAULT -> size == configuration.getDefaultSize();
            case MIN_SIZE -> size >= configuration.getDefaultSize() && size <= 54;
            case MAX_SIZE -> size <= configuration.getDefaultSize();
        };
    }

    @Override
    public OwnerResolver getOwnerResolver() {
        return this.ownerResolver;
    }

    @Override
    public List<String> getSizeTabulation() {
        return switch (configuration.getSizeMode()) {
            case DEFAULT -> Stream.of(configuration.getDefaultSize()).map(String::valueOf).toList();
            case MIN_SIZE -> IntStream.iterate(configuration.getDefaultSize(), n -> n + 1)
                    .limit((54 - configuration.getDefaultSize()) + 1)
                    .filter(n -> n <= 54).mapToObj(String::valueOf).toList();
            case MAX_SIZE -> IntStream.iterate(1, n -> n + 1)
                    .limit((configuration.getDefaultSize()) + 1)
                    .filter(n -> n <= configuration.getDefaultSize()).mapToObj(String::valueOf).toList();
        };
    }

    @Override
    public List<String> getNumVaultsTabulation(CommandSender sender) {
        if(!(sender instanceof Player player)) {
            return List.of();
        }
        User user = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId()).orElseThrow();
        return IntStream.iterate(0, n -> n + 1)
                .limit(this.getVaults(user).size() + 1)
                .filter(n -> n <= this.getVaults(user).size()).mapToObj(String::valueOf).toList();
    }

    @Override
    public Vault getVault(User receiver, int vaultNum) throws IndexOutOfBoundVaultException {
        var list = this.getVaults(receiver);
        if(vaultNum < 0 || vaultNum >= list.size()) {
            throw new IndexOutOfBoundVaultException();
        }
        return list.get(vaultNum);
    }

    @Override
    public Vault getVault(UUID vaultId) {
        return this.vaults.get(vaultId);
    }

    @Override
    public List<Vault> getVaults(User user) {
        return this.vaults.values().stream().filter(vault -> vault.hasAccess(user)).toList();
    }

    @Override
    public VaultOwner generateOwner(String type, User receiver) {
        return this.ownerResolver.resolveOwnerFromUser(type, receiver);
    }

    @Override
    public NamespacedKey getAmountKey() {
        return new NamespacedKey(this.getPlugin(), "zvaults_amount");
    }

    @Override
    public void handleLeftClick(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault) {
        InventoryAction action = event.getAction();
        if(action == InventoryAction.PLACE_ALL) {
            if (slot >= vault.getSize()) {
                return;
            }
            event.setCancelled(vault.isInfinite());
            if(!vault.isInfinite()) {
                return;
            }

            if (this.addItem(event, cursor, vault, player, cursor.getAmount())) {
                event.getView().setCursor(new ItemStack(Material.AIR));
            }

        } else if(action == InventoryAction.PLACE_SOME) {
            this.placeSome(event, cursor, current, slot);
        } else if (action == InventoryAction.SWAP_WITH_CURSOR) {
            if(slot >= vault.getSize()) {
                return;
            }
            if(vault.isInfinite()) {
                if (this.addItem(event, cursor, vault, player, cursor.getAmount())) {
                    event.getView().setCursor(new ItemStack(Material.AIR));
                }
            } else {
                if(this.isSimilar(cursor, current)) {
                    this.placeSome(event, cursor, current, slot);
                } else {
                    ItemStack newCursor = new ItemStack(current.getType(), current.getAmount());
                    event.getInventory().setItem(slot, cursor);
                    event.getView().setCursor(newCursor);
                }
            }
        } else if (action == InventoryAction.PICKUP_ALL) {
            if (slot >= vault.getSize()) {
                return;
            }
            if(!vault.isInfinite()) {
                event.getInventory().setItem(slot, new ItemStack(Material.AIR));
                event.getView().setCursor(new ItemStack(current.getType(), current.getAmount()));
                return;
            }
            int amount = Math.min(current.getMaxStackSize(), this.getAmountFromItem(current) + cursor.getAmount());
            this.changeItemVault(event, player, current, slot, vault, amount);
        }
    }

    @Override
    public void handleRightClick(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault) {
        InventoryAction action = event.getAction();
        if(action == InventoryAction.PICKUP_HALF) {
            if (slot >= vault.getSize()) {
                return;
            }
            if(!vault.isInfinite()) {
                int amount = current.getAmount() == 1 ? 1 : current.getAmount() / 2;
                int newCurrentAmount = current.getAmount() - amount;
                int newCursorAmount = cursor.getAmount() + amount;
                ItemStack newCurrent = newCurrentAmount == 0 ? new ItemStack(Material.AIR) : new ItemStack(current.getType(), newCurrentAmount);
                ItemStack newCursor = newCursorAmount == 0 ? new ItemStack(Material.AIR) : new ItemStack(current.getType(), newCursorAmount);
                event.getInventory().setItem(slot, newCurrent);
                event.getView().setCursor(newCursor);
                return;
            }
            int amount = Math.min(current.getMaxStackSize()/2, this.getAmountFromItem(current));
            this.changeItemVault(event, player, current, slot, vault, amount);
        } else if(action == InventoryAction.PLACE_ONE) {
            this.placeOne(event, player, cursor, current, slot, vault);
        } else if(action == InventoryAction.SWAP_WITH_CURSOR) {
            if(this.isSimilar(cursor, current)) {
                this.placeOne(event, player, cursor, current, slot, vault);
            } else {
                event.getInventory().setItem(slot, cursor);
                event.getView().setCursor(new ItemStack(current.getType(), current.getAmount()));
            }
        }
    }

    @Override
    public void handleShift(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault) {
        if(slot >= vault.getSize() && slot < inventorySize || current == null || current.getType().isAir()) {
            return;
        }
        if(slot >= inventorySize) {
            if(vault.isInfinite()) {
                if (this.addItem(event, current, vault, player, current.getAmount())) {
                    event.setCurrentItem(new ItemStack(Material.AIR));
                }
            } else {
                Inventory inventory = Bukkit.createInventory(null, inventorySize, "");
                for(int i = 0; i < vault.getSize(); i++) {
                    ItemStack item = event.getInventory().getItem(i);
                    if(item == null || item.getType().isAir()) {
                        continue;
                    }

                    inventory.setItem(i, item);
                }

                var notAdded = inventory.addItem(current);
                ItemStack newCurrent = notAdded.isEmpty() ? new ItemStack(Material.AIR) : notAdded.values().iterator().next();
                event.setCurrentItem(newCurrent);
                for (int i = 0; i < vault.getSize(); i++) {
                    ItemStack ref = event.getInventory().getItem(i);
                    ItemStack item = inventory.getItem(i);
                    if (ref != null && !ref.isSimilar(item)
                            || item != null && !item.isSimilar(ref)
                            || ref == null && item != null
                            || ref != null && item == null
                            || ref != null && ref.getAmount() != item.getAmount()) {
                        event.getInventory().setItem(i, item);
                    }
                }
            }

        } else {
            if(vault.isInfinite()) {
                int amount = Math.min(current.getMaxStackSize(), this.getAmountFromItem(current));
                ItemStack toAdd = new ItemStack(current.getType(), amount);
                var notAdded = player.getInventory().addItem(toAdd);
                int rest = notAdded.isEmpty() ? 0 : notAdded.values().iterator().next().getAmount();
                int realAmount = amount - rest;
                this.changeCurrent(event, player, current, slot, vault, realAmount);
            } else {
               var notAdded = player.getInventory().addItem(current);
               ItemStack newCurrent = notAdded.isEmpty() ? new ItemStack(Material.AIR) : notAdded.values().iterator().next();
               event.setCurrentItem(newCurrent);
            }
        }
    }

    @Override
    public void handleDrop(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault, boolean controlDrop) {
        if(slot >= vault.getSize()) {
            return;
        }
        int amount = controlDrop ? Math.min(current.getMaxStackSize(), this.getAmountFromItem(current)) : 1;
        if(vault.isInfinite()) {
            ItemStack toDrop = new ItemStack(current.getType(), amount);
            player.getWorld().dropItem(player.getLocation(), toDrop);
            this.changeCurrent(event, player, current, slot, vault, amount);
        } else {
            ItemStack toDrop = new ItemStack(current.getType(), amount);
            player.getWorld().dropItem(player.getLocation(), toDrop);
            ItemStack newCurrent;
            if(current.getAmount() - amount == 0) {
                newCurrent = new ItemStack(Material.AIR);
            } else {
                newCurrent = new ItemStack(current.getType(), current.getAmount() - amount);
            }
            event.getInventory().setItem(slot, newCurrent);
        }
    }

    @Override
    public void handleNumberKey(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault) {
        if(vault.isInfinite()) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if(hotbarItem == null || hotbarItem.getType().isAir()) {
                return;
            }
            int amountItem = hotbarItem.getAmount();
            if (this.addItem(event, hotbarItem, vault, player, amountItem)) {
                player.getInventory().setItem(event.getHotbarButton(), new ItemStack(Material.AIR));
            }
        } else {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            ItemStack currentItem = (current == null || current.getType().isAir()) ? new ItemStack(Material.AIR) : new ItemStack(current.getType(), current.getAmount());
            player.getInventory().setItem(event.getHotbarButton(), currentItem);
            event.getInventory().setItem(slot, hotbarItem);
        }
    }

    @Override
    public void load() {
        this.vaultService.findAll().forEach(vault -> this.vaults.put(vault.getUniqueId(), vault));
    }

    @Override
    public void save() {
        this.vaults.values().forEach(this.vaultService::save);
    }

    private List<Vault> getVaults(UUID owner) {
        return this.vaults.values().stream().filter(vault -> vault.getOwner().getUniqueId().equals(owner)).collect(Collectors.toList());
    }

    private boolean isSimilar(ItemStack item1, ItemStack item2) {
        if(item1 == null && item2 != null || item1 != null && item2 == null) {
            return false;
        }
        if(item2 == null && item1 == null) {
            return true;
        }

        if(item1.getType() != item2.getType()) {
            return false;
        }
        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();
        if(meta1 == null && meta2 != null || meta1 != null && meta2 == null) {
            return false;
        }
        if(meta1 == null && meta2 == null) {
            return true;
        }
        int customModelData1 = meta1.hasCustomModelData() ? meta1.getCustomModelData() : -1;
        int customModelData2 = meta2.hasCustomModelData() ? meta2.getCustomModelData() : -1;
        return customModelData1 == customModelData2;
    }

    private void changeCurrent(InventoryClickEvent event, Player player, ItemStack current, int slot, Vault vault, int amount) {
        int newAmount = this.getAmountFromItem(current) - amount;
        if(newAmount == 0) {
            event.getInventory().setItem(slot, new ItemStack(Material.AIR));
        } else {
            VaultItem vaultItem = new VaultItem(current, newAmount);
            event.getInventory().setItem(slot, vaultItem.toItem(player, vault.isInfinite()));
        }
    }

    private void placeOne(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, Vault vault) {
        if (slot > vault.getSize()) {
            return;
        }
        if(!vault.isInfinite()) {
            int newCursorAmount = cursor.getAmount() - 1;
            ItemStack newCursor =  newCursorAmount == 0 ? new ItemStack(Material.AIR) : new ItemStack(cursor.getType(), newCursorAmount);
            ItemStack newCurrent = new ItemStack((current == null || current.getType() == Material.AIR) ? cursor.getType() : current.getType(), (current == null || current.getType() == Material.AIR) ? 1 : current.getAmount() + 1);
            event.getInventory().setItem(slot, newCurrent);
            event.getView().setCursor(newCursor);
            return;
        }
        if (this.addItem(event, cursor, vault, player, 1)) {
            int newCursorAmount = cursor.getAmount() - 1;
            event.getView().setCursor(newCursorAmount == 0 ? new ItemStack(Material.AIR) : new ItemStack(cursor.getType(), newCursorAmount));
        }
    }

    private void changeItemVault(InventoryClickEvent event, Player player, ItemStack current, int slot, Vault vault, int amount) {
        int rest = this.getAmountFromItem(current) - amount;
        VaultItem vaultItem;
        if(rest > 0) {
            vaultItem = new VaultItem(current, rest);
            event.getInventory().setItem(slot, vaultItem.toItem(player, vault.isInfinite()));
        } else {
            event.getInventory().setItem(slot, new ItemStack(Material.AIR));
        }
        event.getView().setCursor(new ItemStack(current.getType(), amount));
    }

    private void placeSome(InventoryClickEvent event, ItemStack cursor, ItemStack current, int slot) {
        int currentAmount = current.getAmount();
        int cursorAmount = cursor.getAmount();
        int max = current.getMaxStackSize();
        int newAmount = Math.min(max, currentAmount + cursorAmount);
        int newCursorAmount = cursorAmount - (newAmount - currentAmount);
        ItemStack newCursor =  newCursorAmount == 0 ? new ItemStack(Material.AIR) : new ItemStack(cursor.getType(), newCursorAmount);
        ItemStack newCurrent = new ItemStack(current.getType(), newAmount);
        event.getInventory().setItem(slot, newCurrent);
        event.getView().setCursor(newCursor);
    }

    private boolean addItem(InventoryClickEvent event, ItemStack cursor, Vault vault, Player player, int amountItem) {
        int correspondingslot = event.getInventory().first(cursor.getType());
        if(correspondingslot == -1) {
            correspondingslot = event.getInventory().firstEmpty();
        }
        if(correspondingslot == -1) {
            return false;
        }
        ItemStack item = event.getInventory().getItem(correspondingslot);
        int amount;
        if(item == null || item.getType().isAir()) {
            item = new ItemStack(cursor.getType(), amountItem);
            amount = amountItem;
        } else {
            amount = this.getAmountFromItem(item) + amountItem;
        }
        VaultItem vaultItem = new VaultItem(item, amount);
        event.getInventory().setItem(correspondingslot, vaultItem.toItem(player, vault.isInfinite()));
        return true;
    }

    private void registerResolvers(OwnerResolver ownerResolver) {
        ownerResolver.registerOwnerType("player", ZPlayerOwner.class);
    }
}
