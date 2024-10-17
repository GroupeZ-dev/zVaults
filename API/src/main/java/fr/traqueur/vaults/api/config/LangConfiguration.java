package fr.traqueur.vaults.api.config;

import fr.traqueur.vaults.api.messages.Message;

public interface LangConfiguration extends Configuration {

    String translate(Message message);
}
