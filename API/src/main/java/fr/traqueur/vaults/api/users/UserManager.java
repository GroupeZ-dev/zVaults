package fr.traqueur.vaults.api.users;

import fr.traqueur.vaults.api.managers.Manager;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface UserManager extends Manager {

    String USER_TABLE_NAME = "users";

    void handleJoin(Player player);

    void handleQuit(Player player);

    Optional<User> getUser(String name);

    Optional<User> getUser(UUID uuid);

    void saveUser(User user);

}
