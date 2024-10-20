package fr.traqueur.vaults.storage;

import fr.maxlego08.sarah.*;
import fr.maxlego08.sarah.database.DatabaseType;
import fr.traqueur.vaults.api.VaultsLogger;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.storage.Storage;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class SQLStorage implements Storage {

    private final String prefix;
    private final DatabaseConnection connection;
    private final RequestHelper requester;

    public SQLStorage(VaultsPlugin plugin, DatabaseConfiguration databaseConfiguration) {
        this.prefix = databaseConfiguration.getTablePrefix();
        DatabaseType type = databaseConfiguration.getDatabaseType();
        if(type == DatabaseType.MYSQL) {
            this.connection = new MySqlConnection(databaseConfiguration);
        } else if (type == DatabaseType.SQLITE) {
            this.connection = new SqliteConnection(databaseConfiguration, new File(plugin.getDataFolder(), "storage"));
        } else {
            throw new IllegalArgumentException("Invalid storage type !");
        }

        this.requester = new RequestHelper(this.connection, VaultsLogger::info);
        if (!this.connection.isValid()) {
            VaultsLogger.severe("Unable to connect to database !");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        } else {
            VaultsLogger.success("The database connection is valid ! (" + connection.getDatabaseConfiguration().getHost() + ")");
        }
    }

    @Override
    public <DTO> void save(String tableName, DTO data) {
        this.requester.upsert(this.prefix+tableName, table -> {
            for (RecordComponent recordComponent : data.getClass().getRecordComponents()) {
                try {
                    Field field = data.getClass().getDeclaredField(recordComponent.getName());
                    field.setAccessible(true);
                    Object obj = field.get(data);

                    Column column = null;
                    String name = recordComponent.getName();

                    if(recordComponent.isAnnotationPresent(Column.class)) {
                        column = recordComponent.getAnnotation(Column.class);
                        name = recordComponent.getAnnotation(Column.class).value();
                    }

                    if(column != null && column.primary()) {
                        if(obj instanceof UUID uuid) {
                            table.uuid(name, uuid).primary();
                            continue;
                        }
                        table.object(name, obj).primary();
                    } else {
                        if(obj instanceof UUID uuid) {
                            table.uuid(name, uuid);
                            continue;
                        }
                        table.object(name, obj);
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public <DTO> List<DTO> where(String tableName, Class<DTO> clazz, String[] key, String[] content) {
        return this.requester.select(this.prefix+tableName, clazz, table -> {
            for (int i = 0; i < key.length; i++) {
                table.where(key[i], content[i]);
            }
        });
    }

    @Override
    public void onEnable() {
        this.connection.connect();
        MigrationManager.execute(this.connection, VaultsLogger::info);
    }

    @Override
    public void onDisable() {
        this.connection.disconnect();
    }

    @Override
    public <DTO> void delete(String table, DTO dto) {
        this.requester.delete(this.prefix+table, table1 -> {
            for (RecordComponent recordComponent : dto.getClass().getRecordComponents()) {
                try {
                    Field field = dto.getClass().getDeclaredField(recordComponent.getName());
                    field.setAccessible(true);
                    Object obj = field.get(dto);

                    String name = recordComponent.getName();

                    if(recordComponent.isAnnotationPresent(Column.class)) {
                        name = recordComponent.getAnnotation(Column.class).value();
                    }

                    if(obj instanceof UUID uuid) {
                        table1.where(name, uuid.toString());
                        continue;
                    }
                    table1.where(name, obj);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public <DTO> Collection<DTO> findAll(String table, Class<DTO> dtoClass) {
        return this.requester.selectAll(this.prefix+table, dtoClass);
    }
}