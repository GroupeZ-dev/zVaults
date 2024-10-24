package fr.traqueur.vaults.vaults;

import fr.maxlego08.menu.api.dupe.DupeManager;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.sarah.MigrationManager;
import fr.traqueur.vaults.api.VaultsLogger;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.MainConfiguration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.data.Saveable;
import fr.traqueur.vaults.api.data.VaultDTO;
import fr.traqueur.vaults.api.events.VaultCloseEvent;
import fr.traqueur.vaults.api.events.VaultOpenEvent;
import fr.traqueur.vaults.api.events.VaultUpdateEvent;
import fr.traqueur.vaults.api.exceptions.IndexOutOfBoundVaultException;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.storage.Service;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.*;
import fr.traqueur.vaults.hooks.ZSuperiorOwner;
import fr.traqueur.vaults.storage.migrations.VaultsMigration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

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
    private final Map<UUID, User> targetUserVaultsChoose;

    public ZVaultsManager(VaultsConfiguration configuration) {
        this.configuration = configuration;
        this.ownerResolver = new OwnerResolver();
        this.registerResolvers(this.ownerResolver);

        this.vaults = new HashMap<>();
        this.openedVaults = new HashMap<>();
        this.linkedVaultToInventory = new HashMap<>();
        this.targetUserVaultsChoose = new HashMap<>();

        this.vaultService = new Service<>(this.getPlugin(), VaultDTO.class, new ZVaultRepository(this.ownerResolver), VAULT_TABLE_NAME);
        MigrationManager.registerMigration(new VaultsMigration(VAULT_TABLE_NAME));
    }

    @Override
    public Optional<InventoryDefault> getLinkedInventory(UUID vault) {
        return Optional.ofNullable(this.linkedVaultToInventory.get(vault));
    }

    @Override
    public void saveVault(Vault vault) {
        this.vaultService.save(vault);
    }

    @Override
    public void openVault(User user, Vault vault) {
        VaultOpenEvent event = new VaultOpenEvent(this.getPlugin(), user, vault);
        Bukkit.getPluginManager().callEvent(event);
        if(event.isCancelled()) {
            return;
        }
        this.openedVaults.computeIfAbsent(vault.getUniqueId(), uuid -> new ArrayList<>()).add(user.getUniqueId());
        if(this.linkedVaultToInventory.containsKey(vault.getUniqueId())) {
            user.getPlayer().openInventory(this.linkedVaultToInventory.get(vault.getUniqueId()).getSpigotInventory());
            return;
        }
        vault.setContent(event.getContent());
        this.getPlugin().getInventoryManager().openInventory(user.getPlayer(), "vault_menu");
    }

    @Override
    public void linkVaultToInventory(User user, InventoryDefault inventory) {
        Vault vault = this.getOpenedVault(user);
        if(Configuration.getConfiguration(MainConfiguration.class).isDebug()){
            VaultsLogger.info("Vault " + vault.getUniqueId() + " linked to spigot inventory !");
        }
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
            VaultCloseEvent event = new VaultCloseEvent(this.getPlugin(), user, vault);
            Bukkit.getPluginManager().callEvent(event);
            this.linkedVaultToInventory.remove(vault.getUniqueId());
            if(event.isSave()) {
                this.saveVault(vault);
            }
        }
    }

    @Override
    public void createVault(User creator, VaultOwner owner, int size, int playerVaults, boolean infinite) {
        var vaults = this.getVaults(owner.getUniqueId());
        if(playerVaults != -1 &&  vaults.size() >= playerVaults) {
            creator.sendMessage(Message.MAX_VAULTS_REACHED);
            return;
        }
        Vault vault = new ZVault(owner, this.configuration.getVaultIcon(), size, infinite);
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
    public List<String> getNumVaultsTabulation() {
        return IntStream.iterate(0, n -> n + 1)
                .limit(100)
                .filter(n -> n < 100).mapToObj(String::valueOf).toList();
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
    public void handleLeftClick(InventoryClickEvent event, Player player, ItemStack cursor, int slot, Vault vault) {
        User user = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId()).orElseThrow();
        VaultItem vaultItem = vault.getInSlot(slot);
        if(vault.isInfinite()) {
            ItemStack current = vaultItem.item();
            if(cursor == null || cursor.getType().isAir() && !vaultItem.isEmpty()) {
                int amountToRemove = Math.min(vaultItem.amount(), current.getMaxStackSize());
                this.removeItem(event, player, slot, vault, vaultItem, amountToRemove);
            } else if(!cursor.getType().isAir()) {
                int slotToAdd = this.findCorrespondingSlot(event.getInventory(), cursor, vault);
                if(slotToAdd == -1) {
                    return;
                }
                vaultItem = vault.getInSlot(slotToAdd);
                this.addItem(event, player, slotToAdd, vault, vaultItem, cursor, cursor.getAmount());
            }
        } else {
            InventoryAction action = event.getAction();
            switch (action) {
                case PLACE_ALL -> {
                    var newVaultItem = this.addToVaultItem(user, vault, vaultItem, this.cloneItemStack(cursor), cursor.getAmount());
                    event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
                    event.getView().setCursor(new ItemStack(Material.AIR));
                }

                case PLACE_SOME -> {
                    int amountToAdd = cursor.getAmount();
                    int newAmount = Math.min(vaultItem.item().getMaxStackSize(), vaultItem.amount() + amountToAdd);
                    int restInCursor = amountToAdd - (newAmount - vaultItem.amount());
                    var newVaultItem = this.addToVaultItem(user, vault, vaultItem, this.cloneItemStack(cursor), newAmount - vaultItem.amount());
                    event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
                    ItemStack newCursor = this.cloneItemStack(cursor);
                    if(restInCursor == 0) {
                        newCursor = new ItemStack(Material.AIR);
                    } else {
                        newCursor.setAmount(restInCursor);
                    }
                    event.getView().setCursor(newCursor);
                }

                case SWAP_WITH_CURSOR -> {
                    this.switchWithCursor(event, player, cursor, slot, vault, user, vaultItem);
                }

                case PICKUP_ALL -> {
                    var newVaultItem = this.removeFromVaultItem(user, vault, vaultItem, vaultItem.amount());
                    event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
                    ItemStack toAdd = this.cloneItemStack(vaultItem.item());
                    toAdd.setAmount(vaultItem.amount());
                    event.getView().setCursor(toAdd);
                }
            }
        }
    }

    @Override
    public void handleRightClick(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault) {
        User user = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId()).orElseThrow();
        VaultItem vaultItem = vault.getInSlot(slot);
        if (vault.isInfinite()) {
            if(cursor == null || cursor.getType().isAir() && !vaultItem.isEmpty()) {
                int amountToRemove = Math.min(vaultItem.amount() / 2, vaultItem.item().getMaxStackSize() / 2);
                if(amountToRemove == 0) {
                    amountToRemove = 1;
                }
                this.removeItem(event, player, slot, vault, vaultItem, amountToRemove);
            } else if(!cursor.getType().isAir()) {
                int slotToAdd = this.findCorrespondingSlot(event.getInventory(), cursor, vault);
                if(slotToAdd == -1) {
                    return;
                }
                vaultItem = vault.getInSlot(slotToAdd);
                this.addItem(event, player, slotToAdd, vault, vaultItem, cursor, 1);
            }
        } else {
            InventoryAction action = event.getAction();
            switch (action) {
                case SWAP_WITH_CURSOR -> {
                    this.switchWithCursor(event, player, cursor, slot, vault, user, vaultItem);
                }

                case PICKUP_HALF -> {
                    int halfAmount = vaultItem.amount() / 2;
                    if(halfAmount == 0) {
                        halfAmount = 1;
                    }
                    var newVaultItem = this.removeFromVaultItem(user, vault, vaultItem, halfAmount);
                    event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
                    ItemStack toAdd = this.cloneItemStack(vaultItem.item());
                    toAdd.setAmount(halfAmount);
                    event.getView().setCursor(toAdd);
                }
                case PLACE_ONE -> {
                    var newVaultItem = this.addToVaultItem(user, vault, vaultItem, this.cloneItemStack(cursor), 1);
                    event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
                    ItemStack newCursor = this.cloneItemStack(cursor);
                    if(cursor.getAmount() - 1 == 0) {
                        newCursor = new ItemStack(Material.AIR);
                    } else {
                        newCursor.setAmount(cursor.getAmount() - 1);
                    }
                    event.getView().setCursor(newCursor);
                }
            }
        }
    }

    @Override
    public void handleShift(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault) {
        User user = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId()).orElseThrow();
        if(vault.isInfinite()) {
            if (slot < vault.getSize()) {
                this.shiftClickFromVault(event, player, cursor, current, slot, vault, user);
            } else if (slot >= inventorySize) {
                if(cursor == null || cursor.getType().isAir() && current == null || current.getType().isAir()) {
                    return;
                }
                int slotToAdd = this.findCorrespondingSlot(event.getInventory(), current, vault);
                if(slotToAdd == -1) {
                    return;
                }
                VaultItem vaultItem = vault.getInSlot(slotToAdd);
                var newVaultItem = this.addToVaultItem(user, vault, vaultItem, current, current.getAmount());
                event.getInventory().setItem(slotToAdd, newVaultItem.toItem(player, vault.isInfinite()));
                event.setCurrentItem(new ItemStack(Material.AIR));
            }
        } else {
            if (slot < vault.getSize()) {
                this.shiftClickFromVault(event, player, cursor, current, slot, vault, user);
            } else if (slot >= inventorySize) {
                if(cursor == null || cursor.getType().isAir() && current == null || current.getType().isAir()) {
                    return;
                }
                var virtualInv = Bukkit.createInventory(null, inventorySize, "virtual_inv");
                virtualInv.setContents(event.getInventory().getContents());
                var rest = virtualInv.addItem(current);
                for (int i = 0; i < vault.getSize(); i++) {
                    ItemStack virtual = virtualInv.getItem(i);
                    ItemStack real = event.getInventory().getItem(i);
                    if(this.isDifferent(virtual, real, true)) {
                        var newVaultItem = this.addToVaultItem(user, vault, new VaultItem(new ItemStack(Material.AIR), 1, i), virtual, virtual.getAmount());
                        event.getInventory().setItem(i, newVaultItem.toItem(player, vault.isInfinite()));
                    }
                }
                int newCurrentAmount = rest.values().stream().mapToInt(ItemStack::getAmount).sum();
                ItemStack newCurrent = this.cloneItemStack(current);
                if(newCurrentAmount == 0) {
                    newCurrent = new ItemStack(Material.AIR);
                } else {
                    newCurrent.setAmount(newCurrentAmount);
                }
                player.getInventory().setItem(event.getSlot(), newCurrent);
            }
        }
    }

    @Override
    public void handleDrop(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault, boolean controlDrop) {
        User user = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId()).orElseThrow();
        VaultItem vaultItem = vault.getInSlot(slot);
        if(vaultItem.isEmpty()) {
            return;
        }
        int amountToDrop;
        if(controlDrop) {
            amountToDrop = Math.min(vaultItem.amount(), vaultItem.item().getMaxStackSize());
        } else {
            amountToDrop = 1;
        }

        VaultItem newVaultItem = this.removeFromVaultItem(user, vault, vaultItem, amountToDrop);
        event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
        ItemStack item = this.cloneItemStack(vaultItem.item());
        item.setAmount(amountToDrop);
        player.getWorld().dropItemNaturally(player.getLocation(), item);
    }

    @Override
    public void handleNumberKey(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, int inventorySize, Vault vault) {
        User user = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId()).orElseThrow();
        ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
        VaultItem vaultItem = vault.getInSlot(slot);

        if(vault.isInfinite()) {
            if(vaultItem.isEmpty() && hotbarItem != null && !hotbarItem.getType().isAir()) {
                this.addFromHotbar(event, player, vault, hotbarItem);
            } else if(!vaultItem.isEmpty() && (hotbarItem == null || hotbarItem.getType().isAir())) {
                int amount = Math.min(vaultItem.amount(), vaultItem.item().getMaxStackSize());
                ItemStack toAdd = vaultItem.item().clone();
                toAdd.setAmount(amount);
                player.getInventory().setItem(event.getHotbarButton(), toAdd);
                var newVaultItem = this.removeFromVaultItem(user, vault, vaultItem, amount);
                event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
            } else if (hotbarItem != null && vaultItem.item().isSimilar(hotbarItem)) {
                this.addFromHotbar(event, player, vault, hotbarItem);
            }
        } else {
            if(vaultItem.isEmpty() && hotbarItem != null && !hotbarItem.getType().isAir()) {
                var newVaultItem = this.addToVaultItem(user, vault, new VaultItem(new ItemStack(Material.AIR), 1, slot), hotbarItem, hotbarItem.getAmount());
                event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
                player.getInventory().setItem(event.getHotbarButton(), new ItemStack(Material.AIR));
            } else if(!vaultItem.isEmpty() && (hotbarItem == null || hotbarItem.getType().isAir())) {
                ItemStack newHotbarItem = this.cloneItemStack(vaultItem.item());
                newHotbarItem.setAmount(vaultItem.amount());
                player.getInventory().setItem(event.getHotbarButton(), newHotbarItem);
                var newVaultItem = this.removeFromVaultItem(user, vault, vaultItem, vaultItem.amount());
                event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
            } else if (hotbarItem != null && !this.isDifferent(this.cloneItemStack(vaultItem.item()), hotbarItem, false)) {
                int newAmount = Math.min(vaultItem.amount() + hotbarItem.getAmount(), vaultItem.item().getMaxStackSize());
                int rest = hotbarItem.getAmount() - (newAmount - vaultItem.amount());
                var newVaultItem = this.addToVaultItem(user, vault, vaultItem, this.cloneItemStack(hotbarItem), newAmount - vaultItem.amount());
                event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
                ItemStack newHotbarItem = this.cloneItemStack(hotbarItem);
                if(rest == 0) {
                    newHotbarItem = new ItemStack(Material.AIR);
                } else {
                    newHotbarItem.setAmount(rest);
                }
                player.getInventory().setItem(event.getHotbarButton(), newHotbarItem);
            }
        }

    }

    @Override
    public void deleteVault(Vault vault) {
        this.vaults.remove(vault.getUniqueId());
        this.linkedVaultToInventory.remove(vault.getUniqueId());
        if(this.openedVaults.containsKey(vault.getUniqueId())) {
            var users = this.openedVaults.remove(vault.getUniqueId());
            users.forEach(uuid -> {
                User user = this.getPlugin().getManager(UserManager.class).getUser(uuid).orElseThrow();
                user.sendMessage(Message.VAULT_DELETED);
                user.getPlayer().closeInventory();
            });
        }
        this.getPlugin().getManager(VaultConfigurationManager.class).delete(vault);
        this.vaultService.delete(vault);
    }

    @Override
    public void openVaultChooseMenu(User user, User target) {
        this.targetUserVaultsChoose.put(user.getUniqueId(), target);
        this.getPlugin().getInventoryManager().openInventory(user.getPlayer(), "vaults_choose_menu");
    }

    @Override
    public User getTargetUser(User user) {
        return this.targetUserVaultsChoose.get(user.getUniqueId());
    }

    @Override
    public void closeVaultChooseMenu(User user) {
        this.targetUserVaultsChoose.remove(user.getUniqueId());
    }

    @Override
    public void changeSizeOfVault(User user, Vault vault, int size, Message success, Message transmitted) {
        if(!this.sizeIsAvailable(size)) {
            user.sendMessage(Message.SIZE_NOT_AVAILABLE, Formatter.format("%size%", size));
            return;
        }
        vault.setSize(size);
        vault.getOwner().sendMessage(transmitted, Formatter.format("%size%", size));
        user.sendMessage(success, Formatter.format("%size%", size));
    }

    @Override
    public void load() {
        this.vaultService.findAll().forEach(vault -> this.vaults.put(vault.getUniqueId(), vault));
    }

    @Override
    public void save() {
        this.vaults.values().forEach(this.vaultService::save);
    }

    private boolean isDifferent(ItemStack item1, ItemStack item2, boolean checkAmount) {
        if (item1 == null && item2 == null) {
            return false;
        }

        if (item1 == null || item2 == null) {
            return true;
        }

        if (item1.getType() != item2.getType()) {
            return true;
        }

        if(checkAmount) {
            if (item1.getAmount() != item2.getAmount()) {
                return true;
            }
        }

        if (!item1.hasItemMeta() && !item2.hasItemMeta()) {
            return false;
        }

        if (item1.hasItemMeta() != item2.hasItemMeta()) {
            return true;
        }

        if (item1.hasItemMeta() && item2.hasItemMeta()) {
            if (!item1.getItemMeta().equals(item2.getItemMeta())) {
                return true;
            }
        }
        return false;
    }

    private void shiftClickFromVault(InventoryClickEvent event, Player player, ItemStack cursor, ItemStack current, int slot, Vault vault, User user) {
        if(cursor == null || cursor.getType().isAir() && current == null || current.getType().isAir()) {
            return;
        }

        VaultItem vaultItem = vault.getInSlot(slot);
        int removeAmount = Math.min(vaultItem.amount(), current.getMaxStackSize());
        ItemStack toAdd = this.cloneItemStack(vaultItem.item());
        toAdd.setAmount(removeAmount);
        var rest = player.getInventory().addItem(toAdd);
        if(!rest.isEmpty()) {
            removeAmount -= rest.values().stream().mapToInt(ItemStack::getAmount).sum();
        }
        var newVaultItem = this.removeFromVaultItem(user, vault, vaultItem, removeAmount);
        event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
    }

    private void switchWithCursor(InventoryClickEvent event, Player player, ItemStack cursor, int slot, Vault vault, User user, VaultItem vaultItem) {
        var newVaultItem = this.addToVaultItem(user, vault, new VaultItem(new ItemStack(Material.AIR), 1, slot), cursor, cursor.getAmount());
        event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
        ItemStack toAdd = this.cloneItemStack(vaultItem.item());
        toAdd.setAmount(vaultItem.amount());
        event.getView().setCursor(toAdd);
    }

    private void addFromHotbar(InventoryClickEvent event, Player player, Vault vault, ItemStack hotbarItem) {
        User user = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId()).orElseThrow();
        VaultItem vaultItem;
        int slotToAdd = this.findCorrespondingSlot(event.getInventory(), hotbarItem, vault);
        vaultItem = vault.getInSlot(slotToAdd);
        var newVaultItem = this.addToVaultItem(user, vault, vaultItem, hotbarItem, hotbarItem.getAmount());
        event.getInventory().setItem(slotToAdd, newVaultItem.toItem(player, vault.isInfinite()));
        player.getInventory().setItem(event.getHotbarButton(), new ItemStack(Material.AIR));
    }

    private List<Vault> getVaults(UUID owner) {
        return this.vaults.values().stream().filter(vault -> vault.getOwner().getUniqueId().equals(owner)).collect(Collectors.toList());
    }

    private void addItem(InventoryClickEvent event, Player player, int slot, Vault vault, VaultItem vaultItem, ItemStack cursor, int amountToAdd) {
        User user = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId()).orElseThrow();
        var newVaultItem = this.addToVaultItem(user, vault, vaultItem, cursor, amountToAdd);
        event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
        int newAmount = cursor.getAmount() - amountToAdd;
        if(newAmount == 0) {
            event.getView().setCursor(new ItemStack(Material.AIR));
            return;
        }
        cursor.setAmount(newAmount);
        event.getView().setCursor(cursor);
    }

    private void removeItem(InventoryClickEvent event, Player player, int slot, Vault vault, VaultItem vaultItem, int amountToRemove) {
        User user = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId()).orElseThrow();
        var newVaultItem = this.removeFromVaultItem(user, vault, vaultItem, amountToRemove);
        event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
        ItemStack newCursor = newVaultItem.isEmpty() ? vaultItem.item().clone() : newVaultItem.item().clone();
        newCursor.setAmount(amountToRemove);
        event.getView().setCursor(newCursor);
    }

    private int findCorrespondingSlot(Inventory inventory, ItemStack correspond, Vault vault) {
        for (VaultItem vaultItem : vault.getContent()) {
            if(correspond.isSimilar(vaultItem.item())) {
                return vaultItem.slot();
            }
        }
        return inventory.firstEmpty();
    }

    private VaultItem removeFromVaultItem(User user, Vault vault, VaultItem vaultItem, int amount) {
        int currentAmount = vaultItem.amount();
        VaultItem newVaultItem;
        if(currentAmount - amount == 0) {
            newVaultItem = new VaultItem(new ItemStack(Material.AIR), 1, vaultItem.slot());
        } else {
            newVaultItem = new VaultItem(vaultItem.item(), currentAmount - amount, vaultItem.slot());
        }
        vault.setContent(vault.getContent().stream().map(item -> item.slot() == newVaultItem.slot() ? newVaultItem : item).collect(Collectors.toList()));
        this.sendUpdate(user, vault, newVaultItem);
        return newVaultItem;
    }

    private VaultItem addToVaultItem(User user, Vault vault, VaultItem vaultItem, ItemStack cursor, int amount) {
        int currentAmount = vaultItem.isEmpty() ? 0 : vaultItem.amount();
        VaultItem newVaultItem = new VaultItem(vaultItem.isEmpty() ? cursor : vaultItem.item(), currentAmount + amount, vaultItem.slot());
        vault.setContent(vault.getContent().stream().map(item -> item.slot() == newVaultItem.slot() ? newVaultItem : item).collect(Collectors.toList()));
        this.sendUpdate(user, vault, newVaultItem);
        return newVaultItem;
    }

    private void sendUpdate(User user, Vault vault, VaultItem newVaultItem) {
        VaultUpdateEvent vaultUpdateEvent = new VaultUpdateEvent(this.getPlugin(), user, vault, newVaultItem, newVaultItem.slot());
        Bukkit.getPluginManager().callEvent(vaultUpdateEvent);
    }

    private ItemStack cloneItemStack(ItemStack itemStack) {
        ItemStack clone = itemStack.clone();
        ItemMeta cloneMeta = clone.getItemMeta();
        if(cloneMeta == null) {
            return clone;
        }
        PersistentDataContainer container = cloneMeta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(Bukkit.getServer().getPluginManager().getPlugin("zMenu"), DupeManager.KEY);
        if(container.has(key)) {
            container.remove(key);
        }
        clone.setItemMeta(cloneMeta);
        return clone;
    }

    private void registerResolvers(OwnerResolver ownerResolver) {
        ownerResolver.registerOwnerType("player", ZPlayerOwner.class);
        if(Bukkit.getServer().getPluginManager().getPlugin("SuperiorSkyblock2") != null) {
            ownerResolver.registerOwnerType("superiorskyblock", ZSuperiorOwner.class);
        }
    }
}
