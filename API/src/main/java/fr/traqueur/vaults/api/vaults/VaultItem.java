package fr.traqueur.vaults.api.vaults;

import fr.maxlego08.menu.MenuItemStack;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.serialization.Base64;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public record VaultItem(ItemStack item, int amount) {

    public static VaultItem deserialize(String serialized) {
        String[] parts = serialized.split(":");
        return new VaultItem(Base64.decodeItem(parts[0]), Integer.parseInt(parts[1]));
    }

    public String serialize() {
        return Base64.encodeItem(this.item) + ":" + this.amount;
    }

    public ItemStack toItem(Player player, boolean infinite) {
        this.item.setAmount(infinite ? 1 : this.amount);
        if(infinite) {
            VaultsManager manager = JavaPlugin.getPlugin(VaultsPlugin.class).getManager(VaultsManager.class);

            MenuItemStack item = Configuration.getConfiguration(VaultsConfiguration.class).getIcon("vault_item");
            item.setDisplayName(item.getDisplayName().replace("%material", this.getTranslateName(this.item.getType())));
            item.setMaterial(this.item.getType().toString());
            ItemStack itemStack = item.build(player);
            ItemMeta meta = itemStack.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(manager.getAmountKey(), PersistentDataType.INTEGER, this.amount);
            itemStack.setItemMeta(meta);
            return itemStack;
        }

        return this.item;
    }

    private String getTranslateName(Material material){
        String key;
        if(material.isBlock()){
            String id = material.getKey().getKey();

            key =  "block.minecraft."+id;
        } else if(material.isItem()){
            String id = material.getKey().getKey();

            key =  "item.minecraft."+id;
        } else {
            throw new IllegalArgumentException("Material is not a block or item");
        }

        return new TranslatableComponent(key).toPlainText();

    }

}
