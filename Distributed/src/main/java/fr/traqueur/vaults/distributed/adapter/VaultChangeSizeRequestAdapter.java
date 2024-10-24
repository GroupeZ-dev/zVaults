package fr.traqueur.vaults.distributed.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.traqueur.vaults.api.distributed.requests.VaultChangeSizeRequest;

import java.io.IOException;
import java.util.UUID;

public class VaultChangeSizeRequestAdapter extends TypeAdapter<VaultChangeSizeRequest> {
    @Override
    public void write(JsonWriter jsonWriter, VaultChangeSizeRequest vaultChangeSizeRequest) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("serverUUID").value(vaultChangeSizeRequest.server().toString());
        jsonWriter.name("vaultUUID").value(vaultChangeSizeRequest.vault().toString());
        jsonWriter.name("size").value(vaultChangeSizeRequest.size());
        jsonWriter.endObject();
    }

    @Override
    public VaultChangeSizeRequest read(JsonReader jsonReader) throws IOException {
        UUID serverUUID = null;
        UUID vaultUUID = null;
        int size = 0;
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "serverUUID":
                    serverUUID = UUID.fromString(jsonReader.nextString());
                    break;
                case "vaultUUID":
                    vaultUUID = UUID.fromString(jsonReader.nextString());
                    break;
                case "size":
                    size = jsonReader.nextInt();
                    break;
            }
        }
        jsonReader.endObject();
        return new VaultChangeSizeRequest(serverUUID, vaultUUID, size);
    }
}
