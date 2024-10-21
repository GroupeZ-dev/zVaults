package fr.traqueur.vaults.api.storage;

import fr.traqueur.vaults.api.VaultsPlugin;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Service<T, DTO> {

    private final Class<DTO> dtoClass;
    protected final Repository<T, DTO> repository;
    protected final VaultsPlugin plugin;
    protected final String table;

    public Service(VaultsPlugin plugin, Class<DTO> dtoClass, Repository<T,DTO> repository, String table) {
        this.plugin = plugin;
        this.repository = repository;
        this.dtoClass = dtoClass;
        if(!this.dtoClass.isRecord()) {
            throw new IllegalArgumentException("DTO class must be a record !");
        };
        this.table = table;
    }

    public void delete(T data) {
        plugin.getStorage().delete(table, repository.toDTO(data));
    }

    public List<T> where(String key, String content) {
        return this.where(new String[] {key}, new String[]{content});
    }

    public List<T> where(String[] key, String[] content) {
        return plugin.getStorage().where(table, this.dtoClass, key, content).stream().map(this.repository::toEntity).collect(Collectors.toList());
    }

    public void save(T data) {
        plugin.getStorage().save(table, repository.toDTO(data));
    }

    public String getTable() {
        return this.table;
    }

    public List<T> findAll() {
        return plugin.getStorage().findAll(table, this.dtoClass)
                .stream()
                .map(this.repository::toEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}