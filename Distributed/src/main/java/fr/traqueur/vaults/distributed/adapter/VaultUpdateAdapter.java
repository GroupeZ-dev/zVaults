package fr.traqueur.vaults.distributed.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.traqueur.vaults.api.distributed.VaultUpdateRequest;
import fr.traqueur.vaults.api.serialization.Base64;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;

import java.io.IOException;
import java.util.UUID;

public class VaultUpdateAdapter extends TypeAdapter<VaultUpdateRequest> {

    private final VaultsManager vaultsManager;

    public VaultUpdateAdapter(VaultsManager vaultsManager) {
        this.vaultsManager = vaultsManager;
    }

    @Override
    public void write(JsonWriter jsonWriter, VaultUpdateRequest vaultUpdate) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("server").value(vaultUpdate.server().toString());
        jsonWriter.name("vault").value(vaultUpdate.vault().getUniqueId().toString());
        jsonWriter.name("slot").value(vaultUpdate.slot());
        jsonWriter.name("vaultItem").value(Base64.encodeItem(vaultUpdate.itemStack()));
        jsonWriter.endObject();
    }

    @Override
    public VaultUpdateRequest read(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        UUID server = null;
        UUID vaultUUID = null;
        int slot = -1;
        String vaultItem = null;

        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            switch (name) {
                case "server":
                    server = UUID.fromString(jsonReader.nextString());
                    break;
                case "vault":
                    vaultUUID = UUID.fromString(jsonReader.nextString());
                    break;
                case "slot":
                    slot = jsonReader.nextInt();
                    break;
                case "vaultItem":
                    vaultItem = jsonReader.nextString();
                    break;
            }
        }
        jsonReader.endObject();

        if (server == null || vaultUUID == null || slot == -1 || vaultItem == null) {
            throw new IOException("Missing required fields");
        }

        Vault vault = this.vaultsManager.getVault(vaultUUID);
        return new VaultUpdateRequest(server, vault, Base64.decodeItem(vaultItem), slot);
    }
}
