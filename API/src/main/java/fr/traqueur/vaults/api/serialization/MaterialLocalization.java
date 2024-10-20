package fr.traqueur.vaults.api.serialization;

import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;

public class MaterialLocalization {

    public static String getTranslateName(Material material){
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
