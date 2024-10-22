package fr.traqueur.vaults.distributed.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.traqueur.vaults.api.serialization.Base64;
import fr.traqueur.vaults.api.vaults.VaultItem;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class VaultItemAdaper extends TypeAdapter<VaultItem> {

    @Override
    public void write(JsonWriter jsonWriter, VaultItem itemStack) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("item").value(itemStack.serialize());
        jsonWriter.endObject();
    }

    @Override
    public VaultItem read(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String item = null;

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "item":
                    item = jsonReader.nextString();
                    break;
            }
        }
        jsonReader.endObject();

        if (item == null) {
            throw new IOException("Missing required fields");
        }
        return VaultItem.deserialize(item);
    }
}
