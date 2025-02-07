package fr.traqueur.vaults.vaults;

import fr.maxlego08.menu.api.dupe.DupeManager;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.sarah.MigrationManager;
import fr.traqueur.vaults.api.CompatibilityUtil;
import fr.traqueur.vaults.api.Plugins;
import fr.traqueur.vaults.api.VaultsLogger;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.MainConfiguration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.data.Saveable;
import fr.traqueur.vaults.api.data.VaultDTO;
import fr.traqueur.vaults.api.events.*;
import fr.traqueur.vaults.api.exceptions.IndexOutOfBoundVaultException;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.placeholders.Placeholders;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ZVaultsManager implements VaultsManager, Saveable {
    
    private final OwnerResolver ownerResolver;
    private final Service<Vault, VaultDTO> vaultService;
    private final Map<UUID, Vault> vaults;
    private final Map<UUID, List<UUID>> openedVaults;
    private final Map<UUID, InventoryDefault> linkedVaultToInventory;
    private final Map<UUID, User> targetUserVaultsChoose;

    public ZVaultsManager() {
        this.ownerResolver = new OwnerResolver();
        this.registerResolvers(this.ownerResolver);

        this.vaults = new HashMap<>();
        this.openedVaults = new HashMap<>();
        this.linkedVaultToInventory = new HashMap<>();
        this.targetUserVaultsChoose = new HashMap<>();
        
        this.vaultService = new Service<>(this.getPlugin(), VaultDTO.class, new ZVaultRepository(this,this.ownerResolver), VAULT_TABLE_NAME);
        MigrationManager.registerMigration(new VaultsMigration(VAULT_TABLE_NAME));

        this.getPlugin().getServer().getPluginManager().registerEvents(new ZVaultsListener(this, this.getPlugin().getManager(UserManager.class)), this.getPlugin());

        this.registerPlaceholders();

    }

    private void registerPlaceholders() {
        Placeholders.register("slots_occuped", (player, args) -> {
            var optuser = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId());
            if(optuser.isEmpty()) {
                return "0";
            }
            User user = optuser.get();
            String vaultNumber= args.getFirst();
            try {
                Vault vault = this.getVault(user, Integer.parseInt(vaultNumber));
                return String.valueOf(vault.getContent().stream().filter(vaultItem -> !vaultItem.isEmpty()).count());
            } catch (IndexOutOfBoundVaultException e) {
                return "No Vault found";
            } catch (NumberFormatException e) {
                return "0";
            }
        });

        Placeholders.register("size", (player, args) -> {
            var optuser = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId());
            if(optuser.isEmpty()) {
                return "0";
            }
            User user = optuser.get();
            String vaultNumber= args.getFirst();
            try {
                Vault vault = this.getVault(user, Integer.parseInt(vaultNumber));
                return String.valueOf(vault.getSize());
            } catch (IndexOutOfBoundVaultException e) {
                return "No Vault found";
            } catch (NumberFormatException e) {
                return "0";
            }
        });

        Placeholders.register("slots_empty", (player, args) -> {
            var optuser = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId());
            if(optuser.isEmpty()) {
                return "0";
            }
            User user = optuser.get();
            String vaultNumber = args.getFirst();
            try {
                Vault vault = this.getVault(user, Integer.parseInt(vaultNumber));
                return String.valueOf(vault.getContent().stream().filter(VaultItem::isEmpty).count());
            } catch (IndexOutOfBoundVaultException e) {
                return "No Vault found";
            } catch (NumberFormatException e) {
                return "0";
            }
        });

        Placeholders.register("is_infinite", (player, args) -> {
            var optuser = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId());
            if(optuser.isEmpty()) {
                return "false";
            }
            User user = optuser.get();
            String vaultNumber= args.getFirst();
            try {
                Vault vault = this.getVault(user, Integer.parseInt(vaultNumber));
                return String.valueOf(vault.isInfinite());
            } catch (IndexOutOfBoundVaultException | NumberFormatException e) {
                return "false";
            }
        });
        Placeholders.register("max_stack_size", (player, args) -> {
            var optuser = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId());
            if(optuser.isEmpty()) {
                return "false";
            }
            User user = optuser.get();
            String vaultNumber= args.getFirst();
            try {
                Vault vault = this.getVault(user, Integer.parseInt(vaultNumber));
                return String.valueOf(vault.getMaxStackSize());
            } catch (IndexOutOfBoundVaultException | NumberFormatException e) {
                return "false";
            }
        });
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
        if(Configuration.get(MainConfiguration.class).isDebug()){
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
    public long getNextVaultId(VaultOwner owner) {
        return this.vaults.values().stream()
                .filter(vault -> vault.getOwner().getUniqueId().equals(owner.getUniqueId()))
                .map(Vault::getId)
                .max(Long::compareTo)
                .orElse(0L) + 1;
    }

    @Override
    public boolean idExists(VaultOwner owner, long id) {
        return this.vaults.values().stream()
                .filter(vault -> vault.getOwner().getUniqueId().equals(owner.getUniqueId()))
                .map(Vault::getId)
                .anyMatch(vaultId -> vaultId == id);
    }

    @Override
    public long generateId(VaultOwner owner) {
        long id = this.getNextVaultId(owner);
        while(this.idExists(owner, id)) {
            id++;
        }
        return id;
    }


    @Override
    public void convertVault(UUID playerOwner, int size, boolean infinite, List<ItemStack> content, Material icon) {
        ZPlayerOwner owner = new ZPlayerOwner(playerOwner);
        Vault vault = new ZVault(owner, icon, size, infinite, Configuration.get(VaultsConfiguration.class).getDefaultVaultName(), this.generateId(owner));
        List<VaultItem> vaultItems = new ArrayList<>();
        for (int i = 0; i < content.size(); i++) {
            VaultItem item;
            if(content.get(i) == null) {
               item = new VaultItem(new ItemStack(Material.AIR), 1, i);
            } else {
                item = new VaultItem(content.get(i), content.get(i).getAmount(), i);
            }
            vaultItems.add(item);
        }
        vault.setContent(vaultItems);
        this.vaults.put(vault.getUniqueId(), vault);
        this.saveVault(vault);
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
    public void createVault(User creator, VaultOwner owner, int size, int playerVaults, boolean infinite, boolean silent, long id) {
        var config = Configuration.get(VaultsConfiguration.class);
        var vaults = this.getVaults(owner.getUniqueId());
        if(playerVaults != -1 &&  vaults.size() >= playerVaults) {
            if(!silent) {
                creator.sendMessage(Message.MAX_VAULTS_REACHED);
            }
            return;
        }

        Vault vault = new ZVault(owner, config.getVaultIcon(), size, infinite, config.getDefaultVaultName(), id);
        List<VaultItem> content = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            content.add(new VaultItem(new ItemStack(Material.AIR), 1, i));
        }
        vault.setContent(content);

        vaults.add(vault);
        this.vaultService.save(vault);
        this.vaults.put(vault.getUniqueId(), vault);
        if(!silent) {
            creator.sendMessage(Message.VAULT_CREATED);
            owner.sendMessage(Message.RECEIVE_NEW_VAULT);
        }
        VaultCreateEvent event = new VaultCreateEvent(this.getPlugin(), creator, vault);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void createVault(UUID vaultId, VaultOwner owner, int size, boolean infinite, long id) {
        var config = Configuration.get(VaultsConfiguration.class);
        Vault vault = new ZVault(vaultId, owner, config.getVaultIcon(), size, infinite, config.getDefaultVaultName(), id);
        List<VaultItem> content = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            content.add(new VaultItem(new ItemStack(Material.AIR), 1, i));
        }
        vault.setContent(content);
        this.vaults.put(vaultId, vault);
    }

    @Override
    public boolean sizeIsAvailable(int size) {
        return switch (Configuration.get(VaultsConfiguration.class).getSizeMode()) {
            case DEFAULT -> size == Configuration.get(VaultsConfiguration.class).getDefaultSize();
            case MIN_SIZE -> size >= Configuration.get(VaultsConfiguration.class).getDefaultSize() && size <= 54;
            case MAX_SIZE -> size <= Configuration.get(VaultsConfiguration.class).getDefaultSize();
        };
    }

    @Override
    public OwnerResolver getOwnerResolver() {
        return this.ownerResolver;
    }

    @Override
    public List<String> getSizeTabulation() {
        return switch (Configuration.get(VaultsConfiguration.class).getSizeMode()) {
            case DEFAULT -> Stream.of(Configuration.get(VaultsConfiguration.class).getDefaultSize()).map(String::valueOf).toList();
            case MIN_SIZE -> IntStream.iterate(Configuration.get(VaultsConfiguration.class).getDefaultSize(), n -> n + 1)
                    .limit((54 - Configuration.get(VaultsConfiguration.class).getDefaultSize()) + 1)
                    .filter(n -> n <= 54).mapToObj(String::valueOf).toList();
            case MAX_SIZE -> IntStream.iterate(1, n -> n + 1)
                    .limit((Configuration.get(VaultsConfiguration.class).getDefaultSize()) + 1)
                    .filter(n -> n <= Configuration.get(VaultsConfiguration.class).getDefaultSize()).mapToObj(String::valueOf).toList();
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
        Vault vault = list.stream()
                .filter(vault1 -> vault1.getOwner() instanceof ZPlayerOwner)
                .filter(vault1 -> vault1.getId() == vaultNum)
                .findFirst()
                .orElse(null);
        if(vault != null) {
            return vault;
        }

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
                    var newVaultItem = this.addToVaultItem(user, vault, vaultItem, this.cloneItemStack(cursor), cursor.getAmount(), event);
                    event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
                    CompatibilityUtil.setCursor(event, new ItemStack(Material.AIR));
                }

                case PLACE_SOME -> {
                    int amountToAdd = cursor.getAmount();
                    int newAmount = Math.min(vaultItem.item().getMaxStackSize(), vaultItem.amount() + amountToAdd);
                    int restInCursor = amountToAdd - (newAmount - vaultItem.amount());
                    var newVaultItem = this.addToVaultItem(user, vault, vaultItem, this.cloneItemStack(cursor), newAmount - vaultItem.amount(), event);
                    event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
                    ItemStack newCursor = this.cloneItemStack(cursor);
                    if(restInCursor == 0) {
                        newCursor = new ItemStack(Material.AIR);
                    } else {
                        newCursor.setAmount(restInCursor);
                    }
                   CompatibilityUtil.setCursor(event,newCursor);
                }

                case SWAP_WITH_CURSOR -> {
                    this.switchWithCursor(event, player, cursor, slot, vault, user, vaultItem);
                }

                case PICKUP_ALL -> {
                    var newVaultItem = this.removeFromVaultItem(user, vault, vaultItem, vaultItem.amount());
                    event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
                    ItemStack toAdd = this.cloneItemStack(vaultItem.item());
                    toAdd.setAmount(vaultItem.amount());
                   CompatibilityUtil.setCursor(event,toAdd);
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
                   CompatibilityUtil.setCursor(event,toAdd);
                }
                case PLACE_ONE -> {
                    var newVaultItem = this.addToVaultItem(user, vault, vaultItem, this.cloneItemStack(cursor), 1, event);
                    event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
                    ItemStack newCursor = this.cloneItemStack(cursor);
                    if(cursor.getAmount() - 1 == 0) {
                        newCursor = new ItemStack(Material.AIR);
                    } else {
                        newCursor.setAmount(cursor.getAmount() - 1);
                    }
                   CompatibilityUtil.setCursor(event,newCursor);
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
                if((cursor == null || cursor.getType().isAir()) && (current == null || current.getType().isAir())) {
                    return;
                }
                int slotToAdd = this.findCorrespondingSlot(event.getInventory(), current, vault);
                if(slotToAdd == -1) {
                    return;
                }
                VaultItem vaultItem = vault.getInSlot(slotToAdd);
                var newVaultItem = this.addToVaultItem(user, vault, vaultItem, current, current.getAmount(), event);
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
                        var newVaultItem = this.addToVaultItem(user, vault, new VaultItem(new ItemStack(Material.AIR), 1, i), virtual, virtual.getAmount(), event);
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
                var newVaultItem = this.addToVaultItem(user, vault, new VaultItem(new ItemStack(Material.AIR), 1, slot), hotbarItem, hotbarItem.getAmount(),event);
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
                var newVaultItem = this.addToVaultItem(user, vault, vaultItem, this.cloneItemStack(hotbarItem), newAmount - vaultItem.amount(), event);
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
    public void deleteVault(Vault vault, boolean eventLaunch) {
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
        if(eventLaunch) {
            VaultDeleteEvent event = new VaultDeleteEvent(this.getPlugin(), vault);
            Bukkit.getPluginManager().callEvent(event);
        }
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
        VaultChangeSizeEvent event = new VaultChangeSizeEvent(this.getPlugin(), vault, size);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public ItemStack cloneItemStack(ItemStack itemStack) {
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

    @Override
    public int addItem(Vault vault, ItemStack item) {
        VaultItem vaultItem = vault.getContent()
                .stream()
                .filter(vaultItem1 -> vaultItem1.item().isSimilar(item) && vaultItem1.amount() < this.getMaxStackSize(vault, item))
                .findFirst().orElseGet(() -> vault.getContent()
                        .stream()
                        .filter(VaultItem::isEmpty)
                        .findFirst()
                        .orElse(null));
        if(vaultItem == null) {
            return item.getAmount();
        }
        int amount = item.getAmount();
        int baseAmount = vaultItem.isEmpty() ? 0 : vaultItem.amount();
        int newAmount = vault.isInfinite() ? baseAmount + amount : Math.min(vaultItem.item().getMaxStackSize(), baseAmount + amount);
        vaultItem = new VaultItem(this.cloneItemStack(item), newAmount, vaultItem.slot());
        VaultItem finalVaultItem = vaultItem;
        vault.setContent(vault.getContent().stream().map(vaultItem1 -> vaultItem1.slot() == finalVaultItem.slot() ? finalVaultItem : vaultItem1).collect(Collectors.toList()));
        return Math.max(0, amount - (newAmount - baseAmount));
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
        var newVaultItem = this.addToVaultItem(user, vault, new VaultItem(new ItemStack(Material.AIR), 1, slot), cursor, cursor.getAmount(), event);
        event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
        ItemStack toAdd = this.cloneItemStack(vaultItem.item());
        toAdd.setAmount(vaultItem.amount());
       CompatibilityUtil.setCursor(event,toAdd);
    }

    private void addFromHotbar(InventoryClickEvent event, Player player, Vault vault, ItemStack hotbarItem) {
        User user = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId()).orElseThrow();
        VaultItem vaultItem;
        int slotToAdd = this.findCorrespondingSlot(event.getInventory(), hotbarItem, vault);
        vaultItem = vault.getInSlot(slotToAdd);
        var newVaultItem = this.addToVaultItem(user, vault, vaultItem, hotbarItem, hotbarItem.getAmount(), event);
        event.getInventory().setItem(slotToAdd, newVaultItem.toItem(player, vault.isInfinite()));
        player.getInventory().setItem(event.getHotbarButton(), new ItemStack(Material.AIR));
    }

    private List<Vault> getVaults(UUID owner) {
        return this.vaults.values().stream().filter(vault -> vault.getOwner().getUniqueId().equals(owner)).collect(Collectors.toList());
    }

    private void addItem(InventoryClickEvent event, Player player, int slot, Vault vault, VaultItem vaultItem, ItemStack cursor, int amountToAdd) {
        User user = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId()).orElseThrow();
        var newVaultItem = this.addToVaultItem(user, vault, vaultItem, cursor, amountToAdd, event);
        event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
        int newAmount = cursor.getAmount() - amountToAdd;
        if(newAmount == 0) {
           CompatibilityUtil.setCursor(event,new ItemStack(Material.AIR));
            return;
        }
        cursor.setAmount(newAmount);
        CompatibilityUtil.setCursor(event,cursor);
    }

    private void removeItem(InventoryClickEvent event, Player player, int slot, Vault vault, VaultItem vaultItem, int amountToRemove) {
        User user = this.getPlugin().getManager(UserManager.class).getUser(player.getUniqueId()).orElseThrow();
        var newVaultItem = this.removeFromVaultItem(user, vault, vaultItem, amountToRemove);
        event.getInventory().setItem(slot, newVaultItem.toItem(player, vault.isInfinite()));
        ItemStack newCursor = newVaultItem.isEmpty() ? vaultItem.item().clone() : newVaultItem.item().clone();
        newCursor.setAmount(amountToRemove);
        CompatibilityUtil.setCursor(event,newCursor);
    }

    private int findCorrespondingSlot(Inventory inventory, ItemStack correspond, Vault vault) {
        for (VaultItem vaultItem : vault.getContent()) {
            if(correspond.isSimilar(vaultItem.item()) && vaultItem.amount() < this.getMaxStackSize(vault, vaultItem.item())) {
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

    private VaultItem addToVaultItem(User user, Vault vault, VaultItem vaultItem, ItemStack cursor, int amount, InventoryClickEvent event) {
        int maxStackSize = this.getMaxStackSize(vault, cursor);
        int remainingAmount = amount;
        int currentAmount = vaultItem.isEmpty() ? 0 : vaultItem.amount();

        // Add to current slot up to max stack size
        int amountToAddInCurrent = Math.min(maxStackSize - currentAmount, remainingAmount);
        VaultItem newVaultItem = new VaultItem(vaultItem.isEmpty() ? cursor : vaultItem.item(), currentAmount + amountToAddInCurrent, vaultItem.slot());
        vault.setContent(vault.getContent().stream().map(v -> v.slot() == newVaultItem.slot() ? newVaultItem : v).collect(Collectors.toList()));
        this.sendUpdate(user, vault, newVaultItem);

        remainingAmount -= amountToAddInCurrent;
        if (remainingAmount <= 0) {
            return newVaultItem;
        }

        // Add remaining amount in other empty slots
        for (VaultItem slot : vault.getContent()) {
            if (slot.isEmpty()) {
                int amountToAdd = Math.min(maxStackSize, remainingAmount);
                VaultItem additionalVaultItem = new VaultItem(cursor, amountToAdd, slot.slot());
                vault.setContent(vault.getContent().stream().map(v -> v.slot() == additionalVaultItem.slot() ? additionalVaultItem : v).collect(Collectors.toList()));
                this.sendUpdate(user, vault, additionalVaultItem);
                event.getInventory().setItem(slot.slot(), additionalVaultItem.toItem(user.getPlayer(), vault.isInfinite()));
                remainingAmount -= amountToAdd;
                if (remainingAmount <= 0) break;
            }
        }

        return newVaultItem;
    }

    private void sendUpdate(User user, Vault vault, VaultItem newVaultItem) {
        VaultUpdateEvent vaultUpdateEvent = new VaultUpdateEvent(this.getPlugin(), user, vault, newVaultItem, newVaultItem.slot());
        Bukkit.getPluginManager().callEvent(vaultUpdateEvent);
    }

    private int getMaxStackSize(Vault vault, ItemStack item) {
        return vault.isInfinite() ? (vault.getMaxStackSize() == -1 ? Integer.MAX_VALUE : vault.getMaxStackSize())  : item.getMaxStackSize();
    }

    private void registerResolvers(OwnerResolver ownerResolver) {
        ownerResolver.registerOwnerType("player", ZPlayerOwner.class);
        if(Plugins.SUPERIOR.isEnable()) {
            ownerResolver.registerOwnerType("superiorskyblock", ZSuperiorOwner.class);
        }
    }

}
