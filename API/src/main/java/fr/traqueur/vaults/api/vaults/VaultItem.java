package fr.traqueur.vaults.api.vaults;

import fr.traqueur.vaults.api.serialization.Base64;
import org.bukkit.inventory.ItemStack;

public record VaultItem(ItemStack item, int amount) {

    public static VaultItem deserialize(String serialized) {
        String[] parts = serialized.split(":");
        return new VaultItem(Base64.decodeItem(parts[0]), Integer.parseInt(parts[1]));
    }

    public String serialize() {
        return Base64.encodeItem(this.item) + ":" + this.amount;
    }

    public ItemStack toItem(boolean infinite) {
        this.item.setAmount(infinite ? 1 : this.amount);
        return this.item;
    }

}
