package fr.traqueur.vaults.vaults;

import fr.maxlego08.sarah.MigrationManager;
import fr.traqueur.vaults.api.config.VaultsConfiguration;
import fr.traqueur.vaults.api.data.Saveable;
import fr.traqueur.vaults.api.data.VaultDTO;
import fr.traqueur.vaults.api.exceptions.IndexOutOfBoundVaultException;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.storage.Service;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.*;
import fr.traqueur.vaults.storage.migrations.VaultsMigration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ZVaultsManager implements VaultsManager, Saveable {

    private final VaultsConfiguration configuration;
    private final OwnerResolver ownerResolver;
    private final Service<Vault, VaultDTO> vaultService;
    private final Map<UUID, Vault> vaults;
    private final Map<UUID, List<UUID>> openedVaults;

    public ZVaultsManager(VaultsConfiguration configuration) {
        this.configuration = configuration;
        this.ownerResolver = new OwnerResolver();
        this.registerResolvers(this.ownerResolver);

        this.vaults = new HashMap<>();
        this.openedVaults = new HashMap<>();

        this.vaultService = new Service<>(this.getPlugin(), VaultDTO.class, new ZVaultRepository(this.ownerResolver), VAULT_TABLE_NAME);
        MigrationManager.registerMigration(new VaultsMigration(VAULT_TABLE_NAME));

        this.vaultService.findAll().forEach(vault -> this.vaults.put(vault.getUniqueId(), vault));
    }

    @Override
    public void saveVault(Vault vault) {
        this.vaultService.save(vault);
    }

    @Override
    public void openVault(User user, Vault vault) {
        this.openedVaults.computeIfAbsent(vault.getUniqueId(), uuid -> new ArrayList<>()).add(user.getUniqueId());
        this.getPlugin().getInventoryManager().openInventory(user.getPlayer(), "vault_menu");
    }

    @Override
    public Vault getOpenedVault(User user) {
        return this.vaults.values()
                .stream()
                .filter(vault -> this.openedVaults.getOrDefault(vault.getUniqueId(), Collections.emptyList()).contains(user.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void closeVault(User user, Vault vault) {
        this.openedVaults.computeIfAbsent(vault.getUniqueId(), k -> new ArrayList<>()).remove(user.getUniqueId());
        if(this.openedVaults.getOrDefault(vault.getUniqueId(), Collections.emptyList()).isEmpty()) {
            this.saveVault(vault);
        }
    }

    @Override
    public void createVault(User creator, VaultOwner owner, int size) {
        var vaults = this.getVaults(owner.getUniqueId());
        if(this.configuration.getMaxVaultsByPlayer() != -1 && vaults.size() >= this.configuration.getMaxVaultsByPlayer()) {
            creator.sendMessage(Message.MAX_VAULTS_REACHED);
            return;
        }
        Vault vault = new ZVault(owner, size, configuration.isVaultsInfinity());
        vaults.add(vault);
        this.vaultService.save(vault);
        this.vaults.put(vault.getUniqueId(), vault);
        creator.sendMessage(Message.VAULT_CREATED);
        owner.sendMessage(Message.RECEIVE_NEW_VAULT);
    }

    @Override
    public boolean sizeIsAvailable(int size) {
        if(size % 9 != 0) {
            return false;
        }

        return switch (configuration.getSizeMode()) {
            case DEFAULT -> size == configuration.getDefaultSize();
            case MIN_SIZE -> size >= configuration.getDefaultSize() && size <= 54;
            case MAX_SIZE -> size <= configuration.getDefaultSize();
        };
    }

    @Override
    public OwnerResolver getOwnerResolver() {
        return this.ownerResolver;
    }

    @Override
    public List<String> getSizeTabulation() {
        return switch (configuration.getSizeMode()) {
            case DEFAULT -> Stream.of(configuration.getDefaultSize()).map(String::valueOf).toList();
            case MIN_SIZE -> IntStream.iterate(configuration.getDefaultSize(), n -> n + 9)
                    .limit((54 - configuration.getDefaultSize()) / 9 + 1)
                    .filter(n -> n <= 54).mapToObj(String::valueOf).toList();
            case MAX_SIZE -> IntStream.iterate(9, n -> n + 9)
                    .limit((configuration.getDefaultSize() - 9) / 9 + 1)
                    .filter(n -> n <= configuration.getDefaultSize()).mapToObj(String::valueOf).toList();
        };
    }

    @Override
    public List<String> getNumVaultsTabulation() {
        int maxVaults = this.configuration.getMaxVaultsByPlayer() == -1 ? 20 : this.configuration.getMaxVaultsByPlayer();
        return IntStream.iterate(0, n -> n + 1)
                .limit(maxVaults + 1)
                .filter(n -> n <= maxVaults).mapToObj(String::valueOf).toList();
    }

    @Override
    public Vault getVault(User receiver, int vaultNum) throws IndexOutOfBoundVaultException {
        var list = this.getVaults(receiver);
        if(vaultNum < 0 || vaultNum >= list.size()) {
            throw new IndexOutOfBoundVaultException();
        }
        return list.get(vaultNum);
    }

    @Override
    public List<Vault> getVaults(User user) {
        return this.vaults.values().stream().filter(vault -> vault.hasAccess(user)).toList();
    }

    @Override
    public VaultOwner generateOwner(String type, User receiver) {
        return this.ownerResolver.resolveOwnerFromUser(type, receiver);
    }

    @Override
    public NamespacedKey getAmountKey() {
        return new NamespacedKey(this.getPlugin(), "zvaults_amount");
    }

    @Override
    public void save() {
        this.vaults.values().forEach(this.vaultService::save);
    }

    private List<Vault> getVaults(UUID owner) {
        return this.vaults.values().stream().filter(vault -> vault.getOwner().getUniqueId().equals(owner)).collect(Collectors.toList());
    }

    private void registerResolvers(OwnerResolver ownerResolver) {
        ownerResolver.registerOwnerType("player", ZPlayerOwner.class);
    }
}
