package fr.traqueur.vaults.api.data;

import fr.maxlego08.sarah.Column;

import java.util.UUID;

public record SharedAccessDTO(@Column(value = "unique_id", primary = true) UUID uuid,
                              UUID vaultId,
                              UUID userId){
}
