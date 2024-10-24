package fr.traqueur.vaults.distributed.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.traqueur.vaults.api.distributed.requests.VaultStateRequest;

import java.io.IOException;
import java.util.UUID;

public class VaultStateRequestAdapter extends TypeAdapter<VaultStateRequest> {
    @Override
    public void write(JsonWriter jsonWriter, VaultStateRequest vaultStateRequest) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("serverUUID").value(vaultStateRequest.server().toString());
        jsonWriter.name("vaultUUID").value(vaultStateRequest.vault().toString());
        jsonWriter.name("state").value(vaultStateRequest.state().name());
        jsonWriter.endObject();
    }

    @Override
    public VaultStateRequest read(JsonReader jsonReader) throws IOException {
        UUID serverUUID = null;
        UUID vaultUUID = null;
        VaultStateRequest.State state = null;
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "serverUUID" -> serverUUID = UUID.fromString(jsonReader.nextString());
                case "vaultUUID" -> vaultUUID = UUID.fromString(jsonReader.nextString());
                case "state" -> state = VaultStateRequest.State.valueOf(jsonReader.nextString());
            }
        }
        jsonReader.endObject();
        return new VaultStateRequest(serverUUID, vaultUUID, state);
    }
}
