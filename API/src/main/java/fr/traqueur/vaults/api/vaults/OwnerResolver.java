package fr.traqueur.vaults.api.vaults;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OwnerResolver {

    private final Map<String, Class<? extends VaultOwner>> ownerTypes;

    public OwnerResolver() {
        this.ownerTypes = new HashMap<>();
    }

    public void registerOwnerType(String name, Class<? extends VaultOwner> ownerType) {
        this.ownerTypes.put(name, ownerType);
    }

    public VaultOwner resolveOwner(String name, UUID uniqueId) {
        Class<? extends VaultOwner> ownerType = this.ownerTypes.get(name);
        if (ownerType == null) {
            throw new IllegalArgumentException("Unknown owner type: " + name);
        }
        try {
            return ownerType.getConstructor(UUID.class).newInstance(uniqueId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create owner instance", e);
        }
    }

    public String getType(Class<? extends VaultOwner> aClass) {
        for (Map.Entry<String, Class<? extends VaultOwner>> entry : ownerTypes.entrySet()) {
            if (entry.getValue().equals(aClass)) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Unknown owner type: " + aClass);
    }
}
