package fr.traqueur.vaults.lang;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import fr.traqueur.vaults.api.VaultsLogger;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.config.LangConfiguration;
import fr.traqueur.vaults.api.config.NonLoadable;
import fr.traqueur.vaults.api.messages.Message;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ZLangConfiguration implements LangConfiguration {

    @NonLoadable
    private final VaultsPlugin plugin;
    @NonLoadable
    private final Map<String, YamlDocument> langs;
    @NonLoadable
    private YamlDocument lang;
    @NonLoadable
    private boolean load;

    public ZLangConfiguration(VaultsPlugin plugin) {
        this.plugin = plugin;
        this.langs = new HashMap<>();
        this.load = false;
    }

    @Override
    public String getFile() {
        return "lang.yml";
    }

    public String translate(Message message) {
        return this.lang.getString(message.name().toLowerCase());
    }

    @Override
    public void loadConfig() {
        YamlDocument config = this.getConfig(this.plugin);

        Section langs = config.getSection("langs");
        langs.getKeys().forEach(lang -> {
            String langFile = langs.getString(lang.toString());
            try {
                YamlDocument langDocument = YamlDocument.create(new File(plugin.getDataFolder(), langFile), Objects.requireNonNull(plugin.getResource(langFile)), GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build());
                this.langs.put(lang.toString(), langDocument);
                langDocument.save();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        if(!this.langs.containsKey(config.getString("lang"))) {
            throw new IllegalStateException("Lang not found");
        }
        this.lang = this.langs.get(config.getString("lang"));
        VaultsLogger.success("Loaded " + this.langs.size() + " lang.");

        AtomicBoolean missing = new AtomicBoolean(false);
        this.langs.forEach((lang, value) -> {
            for (Message message : Message.values()) {
                if (!value.contains(message.name().toLowerCase())) {
                    VaultsLogger.severe("Missing message: " + message.name().toLowerCase() + " in lang: " + lang);
                    missing.set(true);
                }
            }
        });

        if(missing.get()) {
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
        }
        this.load = true;
    }

    @Override
    public boolean isLoad() {
        return this.load;
    }
}