package fr.traqueur.vaults.vaults;

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
    private final VaultOwner owner;
    private final List<VaultItem> content;
    private final boolean infinite;
    private Material material;
    private int size;

    public ZVault(VaultOwner owner, Material material, int size, boolean infinite) {
        this(UUID.randomUUID(), owner, material, new ArrayList<>(), size, infinite);
    }

    public ZVault(UUID uniqueId, VaultOwner owner, Material material, List<VaultItem> content, int size, boolean infinite) {
        this.uniqueId = uniqueId;
        this.owner = owner;
        this.material = material;
        this.content = content;
        this.size = size;
        this.infinite = infinite;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public VaultOwner getOwner() {
        return this.owner;
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
}
