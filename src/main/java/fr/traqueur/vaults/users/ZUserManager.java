package fr.traqueur.vaults.users;

import fr.maxlego08.sarah.MigrationManager;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.FirstJoinConfig;
import fr.traqueur.vaults.api.config.VaultPreset;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.data.Saveable;
import fr.traqueur.vaults.api.data.UserDTO;
import fr.traqueur.vaults.api.storage.Service;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.VaultOwner;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.storage.migrations.UserMigration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ZUserManager implements UserManager, Saveable {

    public static final User CONSOLE_USER = new ZUser(UUID.fromString("00000000-0000-0000-0000-000000000000"), "CONSOLE");

    private final Service<User, UserDTO> userService;
    private final Map<UUID, User> users;

    public ZUserManager() {
        this.userService = new Service<>(this.getPlugin(), UserDTO.class, new ZUserRepository(), USER_TABLE_NAME);
        this.users = new HashMap<>();

        MigrationManager.registerMigration(new UserMigration(USER_TABLE_NAME));
        Bukkit.getServer().getPluginManager().registerEvents(new ZUserListener(this), this.getPlugin());
    }

    @Override
    public void handleJoin(Player player) {
        this.generateUser(player);
    }

    @Override
    public void handleQuit(Player player) {
        User user = this.users.remove(player.getUniqueId());
        if(user != null) {
            this.userService.save(user);
        }
    }

    @Override
    public Optional<User> getUser(String name) {
        var opt = this.users.values().stream().filter(user -> user.getName().equalsIgnoreCase(name)).findFirst();
        if(opt.isPresent()) {
            return opt;
        }
        var list = this.userService.where("name", name);
        return cacheUser(list);
    }

    @Override
    public Optional<User> getUser(UUID uuid) {
        var opt = Optional.ofNullable(this.users.get(uuid));
        if(opt.isPresent()) {
            return opt;
        }
        var list = this.userService.where("unique_id", uuid.toString());
        return cacheUser(list);
    }

    @Override
    public void saveUser(User user) {
        this.userService.save(user);
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(this.users.values());
    }

    @Override
    public void tryCreateDefaultVaults(User user) {
        FirstJoinConfig config = Configuration.get(VaultsConfiguration.class).getFirstJoinGiveVault();
        if(!config.enabled()) {
            return;
        }
        VaultsManager vaultsManager = this.getPlugin().getManager(VaultsManager.class);
        VaultOwner owner = vaultsManager.generateOwner("player", user);
        int maxVaults = Configuration.get(VaultsConfiguration.class).getMaxVaultsByOwnerType("player");
        for (VaultPreset vault : config.vaults()) {
            vaultsManager.createVault(user, owner, vault.size(), maxVaults, vault.infinite(), true);
        }
    }

    private void generateUser(Player player) {
        if(this.users.containsKey(player.getUniqueId())) {
            return;
        }
        var list = this.userService.where("unique_id", player.getUniqueId().toString());
        if(list.isEmpty()) {
            var user = new ZUser(player.getUniqueId(), player.getName());
            this.userService.save(user);
            this.users.put(player.getUniqueId(), user);

            this.tryCreateDefaultVaults(user);

            return;
        }
        var user = list.getFirst();
        this.users.put(player.getUniqueId(), user);
    }

    @NotNull
    private Optional<User> cacheUser(List<User> list) {
        if(list.isEmpty()) {
            return Optional.empty();
        }
        var user = list.getFirst();
        this.users.put(user.getUniqueId(), user);

        this.getPlugin().getScheduler().runLaterAsync(() -> {
            User userInner = this.users.remove(user.getUniqueId());
            if(userInner != null) {
                this.userService.save(userInner);
            }
        }, 15, TimeUnit.MINUTES);

        return Optional.of(user);
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {
        this.users.values().forEach(this.userService::save);
    }
}
