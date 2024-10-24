package fr.traqueur.vaults.distributed.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.traqueur.vaults.api.distributed.requests.VaultIconChangeRequest;
import org.bukkit.Material;

import java.io.IOException;
import java.util.UUID;

public class VaultChangeIconRequestAdapter extends TypeAdapter<VaultIconChangeRequest> {
    @Override
    public void write(JsonWriter jsonWriter, VaultIconChangeRequest vaultIconChangeRequest) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("server").value(vaultIconChangeRequest.server().toString());
        jsonWriter.name("vault").value(vaultIconChangeRequest.vault().toString());
        jsonWriter.name("icon").value(vaultIconChangeRequest.icon().name());
        jsonWriter.endObject();
    }

    @Override
    public VaultIconChangeRequest read(JsonReader jsonReader) throws IOException {
        UUID server = null;
        UUID vault = null;
        Material icon = null;

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "server" -> {
                    server = UUID.fromString(jsonReader.nextString());
                }
                case "vault" -> {
                    vault = UUID.fromString(jsonReader.nextString());
                }
                case "icon" -> {
                    icon = Material.getMaterial(jsonReader.nextString());
                }
            }
        }
        jsonReader.endObject();
        return new VaultIconChangeRequest(server, vault, icon);
    }
}
