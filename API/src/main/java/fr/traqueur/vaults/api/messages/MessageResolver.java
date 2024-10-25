package fr.traqueur.vaults.api.messages;

import fr.traqueur.vaults.api.VaultsPlugin;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageResolver {

    private final MiniMessage miniMessage;
    private final BukkitAudiences audiences;

    public MessageResolver(VaultsPlugin plugin) {
        this.miniMessage = MiniMessage.miniMessage();
        this.audiences = BukkitAudiences.create(plugin);
    }

    public void sendMessage(CommandSender sender, String message) {
        this.audiences.sender(sender).sendMessage(miniMessage.deserialize(message));
    }

    public String convertToLegacyFormat(String message) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(miniMessage.deserialize(message));
    }

    public String convertToLegacySectionFormat(String message) {
        return LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(message));
    }

    public Component convertToComponent(String message) {
        return miniMessage.deserialize(message);
    }

    public void close() {
        this.audiences.close();
    }

}
