package fr.traqueur.vaults.api.storage;

import java.util.Collection;
import java.util.List;

public interface Storage {

    <DTO> void save(String table, DTO data);

    <DTO> List<DTO> where(String tableName, Class<DTO> clazz, String[] key, String[] content);

    default <DTO> List<DTO> where(String tableName, Class<DTO> clazz, String key, String content) {
        return this.where(tableName, clazz, new String[] {key}, new String[] {content});
    }

    <DTO> void delete(String table, DTO dto);

    <DTO> Collection<DTO> findAll(String table, Class<DTO> dtoClass);

    void onEnable();

    void onDisable();
}