package fr.traqueur.zvaults.hooks;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.VaultOwner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class ZSuperiorOwner extends VaultOwner {

    public ZSuperiorOwner(User user) {
        super(user);
    }

    public ZSuperiorOwner(UUID uniqueId) {
        super(uniqueId);
    }

    @Override
    public UUID fromUser(User user) {
        return SuperiorSkyblockAPI.getPlayer(user.getUniqueId()).getIsland().getUniqueId();
    }

    @Override
    public boolean isEnable() {
        return Bukkit.getServer().getPluginManager().getPlugin("SuperiorSkyblock2") != null;
    }

    @Override
    public boolean hasAccess(UUID player) {
        return SuperiorSkyblockAPI.getIslandByUUID(this.getUniqueId())
                .getAllPlayersInside()
                .stream()
                .map(SuperiorPlayer::getUniqueId)
                .anyMatch(uuid -> uuid.equals(player));
    }

    @Override
    public void sendMessage(Message message, Formatter... formatters) {
        VaultsPlugin plugin = JavaPlugin.getPlugin(VaultsPlugin.class);
        SuperiorSkyblockAPI.getIslandByUUID(this.getUniqueId())
                .getAllPlayersInside()
                .stream()
                .map(SuperiorPlayer::getUniqueId)
                .map(SuperiorSkyblockAPI::getPlayer)
                .forEach(player ->
                        player.runIfOnline((playerInner) ->
                                plugin.getMessageResolver().sendMessage(playerInner, message.translate(formatters))));
    }
}
