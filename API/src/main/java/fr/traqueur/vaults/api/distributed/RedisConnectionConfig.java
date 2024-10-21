package fr.traqueur.vaults.api.distributed;

import fr.traqueur.vaults.api.config.Loadable;

public record RedisConnectionConfig(String host, int port, String user, String password) implements Loadable {
}
