package fr.traqueur.vaults.distributed.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.traqueur.vaults.api.configurator.ShareType;
import fr.traqueur.vaults.api.distributed.requests.VaultShareRequest;

import java.io.IOException;
import java.util.UUID;

public class VaultShareRequestAdapter extends TypeAdapter<VaultShareRequest> {
    @Override
    public void write(JsonWriter jsonWriter, VaultShareRequest vaultShareRequest) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("server").value(vaultShareRequest.server().toString());
        jsonWriter.name("shared").value(vaultShareRequest.shared().toString());
        jsonWriter.name("vault").value(vaultShareRequest.vault().toString());
        jsonWriter.name("player").value(vaultShareRequest.player().toString());
        jsonWriter.name("type").value(vaultShareRequest.type().name());
        jsonWriter.endObject();
    }

    @Override
    public VaultShareRequest read(JsonReader jsonReader) throws IOException {
        UUID server = null;
        UUID shared = null;
        UUID vault = null;
        UUID player = null;
        ShareType type = null;
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "server":
                    server = UUID.fromString(jsonReader.nextString());
                    break;
                case "shared":
                    shared = UUID.fromString(jsonReader.nextString());
                    break;
                case "vault":
                    vault = UUID.fromString(jsonReader.nextString());
                    break;
                case "player":
                    player = UUID.fromString(jsonReader.nextString());
                    break;
                case "type":
                    type = ShareType.valueOf(jsonReader.nextString());
                    break;
            }
        }
        jsonReader.endObject();
        return new VaultShareRequest(server, shared, vault, player, type);
    }
}
