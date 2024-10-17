package fr.traqueur.vaults.api.data;

import fr.maxlego08.sarah.Column;

import java.util.UUID;

public record UserDTO(@Column(value = "unique_id", primary = true)
                      UUID uniqueId,
                      String name) {
}
