package fr.traqueur.vaults.distributed.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.traqueur.vaults.api.distributed.requests.VaultDeleteRequest;

import java.io.IOException;
import java.util.UUID;

public class VaultDeleteRequestAdapter extends TypeAdapter<VaultDeleteRequest> {
    @Override
    public void write(JsonWriter jsonWriter, VaultDeleteRequest vaultDeleteRequest) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("server").value(vaultDeleteRequest.server().toString());
        jsonWriter.name("vault").value(vaultDeleteRequest.vaultId().toString());
        jsonWriter.endObject();
    }

    @Override
    public VaultDeleteRequest read(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        String server = null;
        String vault = null;
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("server")) {
                server = jsonReader.nextString();
            } else if (name.equals("vault")) {
                vault = jsonReader.nextString();
            }
        }
        jsonReader.endObject();

        if(server == null || vault == null) {
            throw new IOException("Missing fields in json object");
        }

        return new VaultDeleteRequest(UUID.fromString(server), UUID.fromString(vault));
    }
}
