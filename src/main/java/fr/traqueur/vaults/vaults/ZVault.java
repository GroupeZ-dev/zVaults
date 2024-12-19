package fr.traqueur.vaults.vaults;

import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultItem;
import fr.traqueur.vaults.api.vaults.VaultOwner;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ZVault implements Vault {

    private final UUID uniqueId;
    private long id;
    private final VaultOwner owner;
    private final List<VaultItem> content;
    private final boolean infinite;
    private String name;
    private Material material;
    private int size;
    private boolean autoPickup;
    private int maxStackSize;

    public ZVault(VaultOwner owner, Material material, int size, boolean infinite, String name, Long id) {
        this(UUID.randomUUID(), owner, material, size, infinite, name, id);
    }

    public ZVault(UUID uuid, VaultOwner owner, Material material, int size, boolean infinite, String name, Long id) {
        this(uuid, owner, material, new ArrayList<>(), size, infinite, false, Configuration.get(VaultsConfiguration.class).getStackSizeInfiniteVaults(), name, id);
    }

    public ZVault(UUID uniqueId, VaultOwner owner, Material material, List<VaultItem> content, int size, boolean infinite, boolean autoPickup, int maxStackSize, String name, Long id) {
        this.uniqueId = uniqueId;
        this.owner = owner;
        this.material = material;
        this.content = content;
        this.size = size;
        this.infinite = infinite;
        this.autoPickup = autoPickup;
        this.maxStackSize = maxStackSize;
        this.name = name;
        this.id = id;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public VaultOwner getOwner() {
        return this.owner;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public List<VaultItem> getContent() {
        return this.content;
    }

    @Override
    public void setContent(List<VaultItem> content) {
        this.content.clear();
        this.content.addAll(content);
    }

    @Override
    public boolean isInfinite() {
        return this.infinite;
    }

    @Override
    public Material getIcon() {
        return material;
    }

    @Override
    public void setIcon(Material icon) {
        this.material = icon;
    }

    @Override
    public VaultItem getInSlot(int slot) {
        return this.content.stream().filter(item -> item.slot() == slot).findFirst().orElse(new VaultItem(new ItemStack(Material.AIR), 1, slot));
    }

    @Override
    public boolean isAutoPickup() {
        return this.autoPickup;
    }

    @Override
    public void setAutoPickup(boolean autoPickup) {
        this.autoPickup = autoPickup;
    }

    @Override
    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
    }

    @Override
    public int getMaxStackSize() {
        return this.maxStackSize;
    }
}
