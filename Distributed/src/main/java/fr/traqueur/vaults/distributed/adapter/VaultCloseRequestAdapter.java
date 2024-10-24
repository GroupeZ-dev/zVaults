package fr.traqueur.vaults.distributed.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.traqueur.vaults.api.distributed.requests.VaultCloseRequest;
import fr.traqueur.vaults.api.distributed.requests.VaultOpenAckRequest;
import fr.traqueur.vaults.api.vaults.VaultItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VaultCloseRequestAdapter extends TypeAdapter<VaultCloseRequest> {
    @Override
    public void write(JsonWriter jsonWriter, VaultCloseRequest vaultCloseRequest) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("vaultUUID").value(vaultCloseRequest.vault().toString());
        jsonWriter.name("server").value(vaultCloseRequest.server().toString());
        jsonWriter.name("items");
        jsonWriter.beginArray();
        for (VaultItem item : vaultCloseRequest.content()) {
            jsonWriter.beginObject();
            jsonWriter.name("item").value(item.serialize());
            jsonWriter.endObject();
        }
        jsonWriter.endArray();
        jsonWriter.endObject();
    }

    @Override
    public VaultCloseRequest read(JsonReader jsonReader) throws IOException {
        UUID vaultUUID = null;
        UUID server = null;
        List<VaultItem> items = new ArrayList<>();

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "vaultUUID" -> vaultUUID = UUID.fromString(jsonReader.nextString());
                case "server" -> server = UUID.fromString(jsonReader.nextString());
                case "items" -> {
                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        jsonReader.beginObject();
                        String itemSerialized = null;
                        while (jsonReader.hasNext()) {
                            if ("item".equals(jsonReader.nextName())) {
                                itemSerialized = jsonReader.nextString();
                            }
                        }
                        if (itemSerialized != null) {
                            items.add(VaultItem.deserialize(itemSerialized));
                        }
                        jsonReader.endObject();
                    }
                    jsonReader.endArray();
                }
                default -> jsonReader.skipValue();
            }
        }
        jsonReader.endObject();

        if (vaultUUID == null || server == null) {
            throw new IOException("Missing required fields in VaultOpenAckRequest");
        }

        return new VaultCloseRequest(server, vaultUUID, items);
    }
}
