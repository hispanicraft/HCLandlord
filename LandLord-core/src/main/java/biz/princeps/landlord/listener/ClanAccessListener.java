package biz.princeps.landlord.listener;

import biz.princeps.landlord.api.ILandLord;
import biz.princeps.landlord.api.IOwnedLand;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ClanAccessListener extends BasicListener {

    public ClanAccessListener(ILandLord plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        syncOwnedLands(event.getPlayer());
        syncLocation(event.getPlayer().getLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        if (event.getFrom().getWorld() == to.getWorld()
                && event.getFrom().getChunk().getX() == to.getChunk().getX()
                && event.getFrom().getChunk().getZ() == to.getChunk().getZ()) {
            return;
        }
        syncLocation(to);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() != null) {
            syncLocation(event.getTo());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            syncLocation(event.getClickedBlock().getLocation());
        } else {
            syncLocation(event.getPlayer().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        syncLocation(event.getRightClicked().getLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event) {
        syncLocation(event.getBlock().getLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent event) {
        syncLocation(event.getBlockPlaced().getLocation());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player responsible = getResponsiblePlayer(event.getDamager());
        if (responsible == null) {
            return;
        }
        syncLocation(event.getEntity().getLocation());
    }

    private void syncOwnedLands(Player player) {
        for (IOwnedLand land : plugin.getWGManager().getRegions(player.getUniqueId())) {
            if (land.isClanAccessEnabled()) {
                land.refreshAccessMembers();
            }
        }
    }

    private void syncLocation(Location location) {
        if (location == null) {
            return;
        }

        IOwnedLand land = plugin.getWGManager().getRegion(location);
        if (land != null && land.isClanAccessEnabled()) {
            land.refreshAccessMembers();
        }
    }

    private Player getResponsiblePlayer(Entity entity) {
        if (entity instanceof Player) {
            return (Player) entity;
        }
        if (!(entity instanceof Projectile)) {
            return null;
        }

        ProjectileSource source = ((Projectile) entity).getShooter();
        if (source instanceof Player) {
            return (Player) source;
        }
        return null;
    }
}
