package fr.traqueur.vaults.hooks;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import fr.traqueur.vaults.api.VaultsPlugin;
import fr.traqueur.vaults.api.messages.Formatter;
import fr.traqueur.vaults.api.messages.Message;
import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.vaults.VaultOwner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class ZPartyOwner extends VaultOwner {

    public ZPartyOwner(User user) {
        super(user);
    }

    public ZPartyOwner(UUID uniqueId) {
        super(uniqueId);
    }

    @Override
    public UUID fromUser(User user) {
        return Parties.getApi().getPartyOfPlayer(user.getUniqueId()).getId();
    }

    @Override
    public boolean hasAccess(UUID player) {
        return Parties.getApi().getParty(this.getUniqueId()).getOnlineMembers().stream().anyMatch(member -> member.getPlayerUUID().equals(player));
    }

    @Override
    public void sendMessage(Message message, Formatter... formatters) {
        PartiesAPI api = Parties.getApi();
        VaultsPlugin plugin = JavaPlugin.getPlugin(VaultsPlugin.class);
        api.getParty(this.getUniqueId()).getOnlineMembers().forEach(member -> {
            Player player = Bukkit.getPlayer(member.getPlayerUUID());
            plugin.getMessageResolver().sendMessage(player, message.translate(formatters));
        });
    }
}
