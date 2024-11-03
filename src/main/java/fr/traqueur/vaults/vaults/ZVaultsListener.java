package fr.traqueur.vaults.vaults;

import fr.traqueur.vaults.api.users.User;
import fr.traqueur.vaults.api.users.UserManager;
import fr.traqueur.vaults.api.vaults.Vault;
import fr.traqueur.vaults.api.vaults.VaultsManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ZVaultsListener implements Listener {

    private final UserManager userManager;
    private final VaultsManager vaultsManager;

    public ZVaultsListener(VaultsManager vaultsManager, UserManager userManager) {
        this.vaultsManager = vaultsManager;
        this.userManager = userManager;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if(event.getEntity().getType() != EntityType.PLAYER) return;
        Player player = (Player) event.getEntity();
        this.userManager.getUser(player.getUniqueId()).ifPresent(user ->{
            List<Vault> vaults = this.vaultsManager.getVaults(user).stream().filter(Vault::isAutoPickup).toList();
            ItemStack item = event.getItem().getItemStack();
            int remaining = item.getAmount();
            for (Vault vault : vaults) {
                remaining = this.vaultsManager.addItem(vault, item);
                System.out.println("remaining: " + remaining);
                if(remaining == 0) {
                    event.setCancelled(true);
                    event.getItem().remove();
                    return;
                }
            }
            if(remaining != 0) {
                System.out.println("remaining: " + remaining);
                event.setCancelled(true);
                item.setAmount(remaining);
                player.getInventory().addItem(item);
                event.getItem().remove();
            }
        });
    }

}
