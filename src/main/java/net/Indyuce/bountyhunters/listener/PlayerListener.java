package net.Indyuce.bountyhunters.listener;

import net.Indyuce.bountyhunters.BountyHunters;
import net.Indyuce.bountyhunters.api.player.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void a(PlayerJoinEvent event) {
        BountyHunters.getInstance().getPlayerDataManager().login(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void b(PlayerQuitEvent event) {
        BountyHunters.getInstance().getPlayerDataManager().logout(event.getPlayer());
    }

    @EventHandler
    public void c(PlayerDeathEvent event) {
        if (!event.getEntity().hasMetadata("NPC")) PlayerData.get(event.getEntity()).setIllegalKillStreak(0);
    }
}
