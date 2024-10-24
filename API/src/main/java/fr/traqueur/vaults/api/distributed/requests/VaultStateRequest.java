package fr.traqueur.vaults.api.distributed.requests;

import java.util.UUID;

public record VaultStateRequest(UUID server, UUID vault, State state) {

    public enum State {
        OPEN,
        CLOSE
    }

}
