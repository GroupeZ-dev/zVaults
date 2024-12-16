package fr.traqueur.vaults.api.data;

import fr.maxlego08.sarah.Column;

import java.util.UUID;

public record VaultDTO(@Column(value = "unique_id", primary = true) UUID uniqueId,
                       UUID owner,
                       String ownerType,
                       String icon,
                       @Column(value = "content", type = "LONGTEXT") String content,
                       int size,
                       boolean infinite,
                       Boolean autoPickup,
                       Integer maxStackSize,
                       String name) {
}
