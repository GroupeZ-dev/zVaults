package fr.traqueur.vaults.api.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import fr.traqueur.vaults.api.VaultsPlugin;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public interface Configuration {

    Map<Class<?>, Configuration> REGISTRY = new HashMap<>();

    static <I extends Configuration, T extends I> I registerConfiguration(Class<I> clazz, T configuration) {
        REGISTRY.put(clazz, configuration);
        return configuration;
    }

    static <T extends Configuration> T get(Class<T> clazz) {
        var config = REGISTRY.get(clazz);
        if(clazz.isInstance(config)) {
            return clazz.cast(config);
        } else {
            throw new IllegalArgumentException("Configuration not found for class " + clazz.getName());
        }
    }

    String getFile();

    default YamlDocument getConfig(JavaPlugin plugin) {
        try {
            return YamlDocument.create(new File(plugin.getDataFolder(), this.getFile()), Objects.requireNonNull(plugin.getResource(this.getFile())), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    void loadConfig();

    boolean isLoad();

    default void load() {
        this.autoLoadConfig();
        this.loadConfig();
    }

    default void autoLoadConfig() {
        VaultsPlugin plugin = JavaPlugin.getPlugin(VaultsPlugin.class);
        YamlDocument configuration = this.getConfig(plugin);
        for (Field field : this.getClass().getDeclaredFields()) {

            if (field.isAnnotationPresent(fr.traqueur.vaults.api.config.NonLoadable.class)) continue;

            field.setAccessible(true);

            try {

                String configKey = toLinearCase(field.getName());

                if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                    field.setBoolean(this, configuration.getBoolean(configKey));
                } else if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
                    field.setInt(this, configuration.getInt(configKey));
                } else if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
                    field.setLong(this, configuration.getLong(configKey));
                } else if (field.getType().equals(String.class)) {
                    field.set(this, configuration.getString(configKey));
                } else if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
                    field.set(this, configuration.getDouble(configKey));
                } else if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
                    field.set(this, configuration.getFloat(configKey));
                } else if (field.getType().equals(BigDecimal.class)) {
                    field.set(this, new BigDecimal(configuration.getString(configKey, "0")));
                } else if(field.getType().equals(Material.class)) {
                    field.set(this, Material.valueOf(configuration.getString(configKey, "STONE").toUpperCase()));
                } else if (field.getType().isEnum()) {
                    Class<? extends Enum> enumType = (Class<? extends Enum>) field.getType();
                    field.set(this, Enum.valueOf(enumType, configuration.getString(configKey, "").toUpperCase()));
                } else if (field.getType().equals(List.class)) {

                    Type genericFieldType = field.getGenericType();
                    if (genericFieldType instanceof ParameterizedType type) {
                        Class<?> fieldArgClass = (Class<?>) type.getActualTypeArguments()[0];

                        if (Loadable.class.isAssignableFrom(fieldArgClass)) {
                            field.set(this, loadObjects(plugin.getLogger(), fieldArgClass, configuration.getMapList(configKey)));
                            continue;
                        } else if (NonLoadable.class.isAssignableFrom(fieldArgClass)) {
                            continue;
                        }
                    }

                    field.set(this, configuration.getStringList(configKey));
                } else {
                    Section configurationSection = configuration.getSection(configKey);
                    if (configurationSection == null) continue;
                    Map<String, Object> map = new HashMap<>();
                    configurationSection.getKeys().forEach(key -> map.put(key.toString(), configurationSection.get(key.toString())));
                    field.set(this, createInstanceFromMap(plugin.getLogger(), ((Class<?>) field.getGenericType()).getConstructors()[0], map));
                }
            } catch (Exception exception) {
                plugin.getLogger().severe("An error with loading field " + field.getName() + ": " + exception.getMessage());
            }
        }
    }

    private List<Object> loadObjects(Logger logger, Class<?> fieldArgClass, List<Map<?, ?>> maps) {
        Constructor<?> constructor = fieldArgClass.getConstructors()[0];
        return maps.stream().map(map -> createInstanceFromMap(logger, constructor, map)).collect(Collectors.toList());
    }

    private Object createInstanceFromMap(Logger logger, Constructor<?> constructor, Map<?, ?> map) {
        try {
            Object[] arguments = new Object[constructor.getParameterCount()];
            java.lang.reflect.Parameter[] parameters = constructor.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Class<?> paramType = parameters[i].getType();
                String paramName = parameters[i].getName();
                Object value = map.get(toLinearCase(paramName));

                if (value != null) {
                    try {
                        if (paramType.isArray()) {
                            Class<?> componentType = paramType.getComponentType();
                            List<?> list = (List<?>) value;
                            Object array = Array.newInstance(componentType, list.size());
                            for (int j = 0; j < list.size(); j++) {
                                Object element = list.get(j);
                                element = convertToRequiredType(logger, element, componentType);
                                Array.set(array, j, element);
                            }
                            value = array;
                        } else {
                            value = convertToRequiredType(logger, value, paramType);
                        }
                    } catch (Exception exception) {
                        logger.log(Level.SEVERE, String.format("Error converting value '%s' for parameter '%s' to type '%s'", value, paramName, paramType.getName()), exception);
                    }
                }

                arguments[i] = value;
            }
            return constructor.newInstance(arguments);
        } catch (Exception exception) {
            logger.log(Level.SEVERE, String.format("Failed to create instance from map with constructor %s", constructor), exception);
            logger.log(Level.SEVERE, String.format("Constructor parameters: %s", (Object) constructor.getParameters()));
            logger.log(Level.SEVERE, String.format("Map content: %s", map));
            throw new RuntimeException("Failed to create instance from map with constructor " + constructor, exception);
        }
    }

    private Object convertToRequiredType(Logger logger, Object value, Class<?> type) {
        if (value == null) {
            return null;
        } else if (type.isEnum()) {
            try {
                return Enum.valueOf((Class<Enum>) type, (String) value);
            } catch (IllegalArgumentException exception) {
                logger.log(Level.SEVERE, String.format("Failed to convert '%s' to enum type '%s'", value, type.getName()), exception);
            }
        } else if (type == BigDecimal.class) {
            try {
                return new BigDecimal(value.toString());
            } catch (NumberFormatException exception) {
                logger.log(Level.SEVERE, String.format("Failed to convert '%s' to BigDecimal", value), exception);
            }
        } else if (type == UUID.class) {
            try {
                return UUID.fromString((String) value);
            } catch (IllegalArgumentException exception) {
                logger.log(Level.SEVERE, String.format("Failed to convert '%s' to UUID", value), exception);
            }
        } else if (type == Integer.class || type == int.class) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                logger.log(Level.SEVERE, String.format("Failed to convert '%s' to Integer", value), e);
                throw e;
            }
        } else if (type == Double.class || type == double.class) {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException exception) {
                logger.log(Level.SEVERE, String.format("Failed to convert '%s' to Double", value), exception);
            }
        } else if (type == Long.class || type == long.class) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException exception) {
                logger.log(Level.SEVERE, String.format("Failed to convert '%s' to Long", value), exception);
            }
        } else if (type == Boolean.class || type == boolean.class) {
            try {
                return Boolean.parseBoolean(value.toString());
            } catch (Exception exception) {
                logger.log(Level.SEVERE, String.format("Failed to convert '%s' to Boolean", value), exception);
            }
        } else if (type == Float.class || type == float.class) {
            try {
                return Float.parseFloat(value.toString());
            } catch (NumberFormatException exception) {
                logger.log(Level.SEVERE, String.format("Failed to convert '%s' to Float", value), exception);
            }
        } else if (type == Material.class) {
            try {
                return Material.valueOf(value.toString());
            } catch (IllegalArgumentException exception) {
                logger.log(Level.SEVERE, String.format("Failed to convert '%s' to Material", value), exception);
            }
        }
        return value;
    }

    private String toLinearCase(String camelCaseStr) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < camelCaseStr.length(); i++) {
            char currentChar = camelCaseStr.charAt(i);
            if (Character.isUpperCase(currentChar)) {
                if (i != 0) {
                    result.append('-');
                }
                result.append(Character.toLowerCase(currentChar));
            } else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }

}