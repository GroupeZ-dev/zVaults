package fr.traqueur.vaults.configurator;

import fr.maxlego08.sarah.MigrationManager;
import fr.traqueur.vaults.api.config.Configuration;
import fr.traqueur.vaults.api.config.InvitePlayerMenuConfiguration;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.configurator.SharedAccess;
import fr.traqueur.vaults.api.configurator.VaultConfigurationManager;
import fr.traqueur.vaults.api.data.Saveable;
import fr.traqueur.vaults.api.data.SharedAccessDTO;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.storage.Service;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import fr.traqueur.vaults.configurator.access.ZSharedAccess;
import fr.traqueur.vaults.configurator.access.ZSharedAccessRepository;
import fr.traqueur.vaults.storage.migrations.SharedAccessMigration;
import net.wesjd.anvilgui.AnvilGUI;

import java.util.*;

public class ZVaultConfigurationManager implements VaultConfigurationManager, Saveable {

    private final Map<UUID, List<UUID>> openedConfigVaults;
    private final Map<UUID, List<UUID>> openedAccessManagerVaults;
    private final Map<UUID, List<SharedAccess>> sharedAccesses;

    private final Service<SharedAccess, SharedAccessDTO> sharedAccessService;

    public ZVaultConfigurationManager() {
        this.openedConfigVaults = new HashMap<>();
        this.sharedAccesses = new HashMap<>();
        this.openedAccessManagerVaults = new HashMap<>();

        this.sharedAccessService = new Service<>(this.getPlugin(), SharedAccessDTO.class, new ZSharedAccessRepository(this.getPlugin()), SHARED_ACCESS_TABLE);
        MigrationManager.registerMigration(new SharedAccessMigration(SHARED_ACCESS_TABLE));
    }

    @Override
    public void openVaultConfig(User user, Vault vault) {
        this.openedConfigVaults.computeIfAbsent(vault.getUniqueId(), uuid -> new ArrayList<>()).add(user.getUniqueId());
        this.getPlugin().getInventoryManager().openInventory(user.getPlayer(), "vault_config_menu");
    }

    @Override
    public void closeVaultConfig(User user) {
        this.openedConfigVaults.values().forEach(uuids -> uuids.remove(user.getUniqueId()));
    }

    @Override
    public boolean hasAccess(Vault vault, User user) {
        if(!this.sharedAccesses.containsKey(vault.getUniqueId())) {
            return false;
        }
        return this.sharedAccesses.get(vault.getUniqueId()).stream().anyMatch(sharedAccess -> sharedAccess.getUser().getUniqueId().equals(user.getUniqueId()));
    }

    @Override
    public Vault getOpenedConfig(User user) {
        UUID vaultId = this.openedConfigVaults.keySet().stream().filter(uuid -> this.openedConfigVaults.get(uuid).contains(user.getUniqueId())).findFirst().orElseThrow();
        return this.getPlugin().getManager(VaultsManager.class).getVault(vaultId);
    }

    @Override
    public void openInvitationMenu(User user, Vault vault) {
        this.closeVaultConfig(user);
        InvitePlayerMenuConfiguration config = Configuration.getConfiguration(VaultsConfiguration.class).getInvitePlayerMenuConfiguration();
        UserManager userManager = this.getPlugin().getManager(UserManager.class);
        new AnvilGUI.Builder()
                .onClick((slot, stateSnapshot) -> {
                    if(slot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }
                    String name = stateSnapshot.getText();
                    if(name.equals(user.getName())) {
                        return List.of(AnvilGUI.ResponseAction.replaceInputText(config.tryAgainMessage()));
                    }
                    Optional<User> target = userManager.getUser(name);
                    return target.map(value -> List.of(AnvilGUI.ResponseAction.run(() -> {
                        this.addSharedAccess(user, vault, value);
                    }), AnvilGUI.ResponseAction.close())).orElseGet(() -> List.of(AnvilGUI.ResponseAction.replaceInputText(config.tryAgainMessage())));
                })
                .text(config.startMessage())
                .title(config.title())
                .plugin(this.getPlugin())
                .open(user.getPlayer());
    }

    @Override
    public void openAccessManagerMenu(User user, Vault vault) {
        this.closeVaultConfig(user);
        this.openedAccessManagerVaults.computeIfAbsent(vault.getUniqueId(), uuid -> new ArrayList<>()).add(user.getUniqueId());
        this.getPlugin().getInventoryManager().openInventory(user.getPlayer(), "vault_access_manager_menu");
    }

    @Override
    public void closeAccessManagerMenu(User user) {
        this.openedAccessManagerVaults.values().forEach(uuids -> uuids.remove(user.getUniqueId()));
    }

    @Override
    public Vault getOpenedAccessManager(User user) {
        UUID vaultId = this.openedAccessManagerVaults.keySet().stream().filter(uuid -> this.openedAccessManagerVaults.get(uuid).contains(user.getUniqueId())).findFirst().orElseThrow();
        return this.getPlugin().getManager(VaultsManager.class).getVault(vaultId);
    }

    @Override
    public List<User> getWhoCanAccess(Vault vault) {
        if(!this.sharedAccesses.containsKey(vault.getUniqueId())) {
            return new ArrayList<>();
        }
        return this.sharedAccesses.getOrDefault(vault.getUniqueId(), new ArrayList<>()).stream().map(SharedAccess::getUser).toList();
    }

    @Override
    public void removeAccess(User user, Vault vault, User value) {
        if(!this.hasAccess(vault, value)) {
            return;
        }
        this.sharedAccesses.get(vault.getUniqueId()).removeIf(sharedAccess -> {
            if(!sharedAccess.getUser().getUniqueId().equals(value.getUniqueId())) {
                return false;
            }
            this.sharedAccessService.delete(sharedAccess);
            user.sendMessage(Message.SUCCESSFULLY_REMOVED_ACCESS_TO_VAULT, Formatter.format("%player%", value.getName()));
            return true;
        });
    }

    @Override
    public void delete(Vault vault) {
        var sharedAccess = this.sharedAccesses.remove(vault.getUniqueId());
        if(this.openedAccessManagerVaults.containsKey(vault.getUniqueId())) {
            var list = this.openedAccessManagerVaults.remove(vault.getUniqueId());
            list.forEach(uuid -> {
                User user = this.getPlugin().getManager(UserManager.class).getUser(uuid).orElseThrow();
                user.sendMessage(Message.VAULT_DELETED);
                user.getPlayer().closeInventory();
            });
        }
        if(this.openedConfigVaults.containsKey(vault.getUniqueId())) {
            var list = this.openedConfigVaults.remove(vault.getUniqueId());
            list.forEach(uuid -> {
                User user = this.getPlugin().getManager(UserManager.class).getUser(uuid).orElseThrow();
                user.sendMessage(Message.VAULT_DELETED);
                user.getPlayer().closeInventory();
            });
        }
        if(sharedAccess != null) {
            sharedAccess.forEach(this.sharedAccessService::delete);
        }
    }

    @Override
    public void load() {
        this.sharedAccessService.findAll().forEach(sharedAccess -> this.sharedAccesses.computeIfAbsent(sharedAccess.getVault().getUniqueId(), uuid -> new ArrayList<>()).add(sharedAccess));
    }

    @Override
    public void save() {
        this.sharedAccesses.values().forEach(sharedAccesses -> sharedAccesses.forEach(this.sharedAccessService::save));
    }

    private void addSharedAccess(User user, Vault vault, User value) {
        if(this.hasAccess(vault, value)) {
            user.sendMessage(Message.ALREADY_ACCESS_TO_VAULT, Formatter.format("%player%", value.getName()));
            return;
        }
        SharedAccess sharedAccess = new ZSharedAccess(UUID.randomUUID(), vault, value);
        this.sharedAccesses.computeIfAbsent(vault.getUniqueId(), uuid -> new ArrayList<>()).add(sharedAccess);
        this.sharedAccessService.save(sharedAccess);
        user.sendMessage(Message.SUCCESSFULLY_ADDED_ACCESS_TO_VAULT, Formatter.format("%player%", value.getName()));
    }
}
