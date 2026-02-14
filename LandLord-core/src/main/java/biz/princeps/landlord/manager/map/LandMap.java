package biz.princeps.landlord.manager.map;

import biz.princeps.landlord.api.ILandLord;
import biz.princeps.landlord.api.IOwnedLand;
import biz.princeps.landlord.api.IWorldGuardManager;
import biz.princeps.landlord.util.MapConstants;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * File created by jcdesimp on 3/1/14. updated on 23/09/17 by SpatiumPrinceps
 */
public class LandMap {

    private final IWorldGuardManager wg;
    private final int radius;
    private final String ownSymbol;
    private final String friendsSymbol;
    private final String foreignSymbol;
    private final String background1;
    private final String background2;
    private final String middleSymbol;
    private final String header;
    private final String yours;
    private final String friends;
    private final String others;

    LandMap(Player p, ILandLord plugin, MapConstants cons) {
        this.wg = plugin.getWGManager();
        this.ownSymbol = plugin.getConfig().getString("CommandSettings.Map.symbols.yours");
        this.friendsSymbol = plugin.getConfig().getString("CommandSettings.Map.symbols.friends");
        this.foreignSymbol = plugin.getConfig().getString("CommandSettings.Map.symbols.others");
        this.background1 = plugin.getConfig().getString("CommandSettings.Map.symbols.background1", "#");
        this.background2 = plugin.getConfig().getString("CommandSettings.Map.symbols.background2", ".");
        this.middleSymbol = plugin.getConfig().getString("CommandSettings.Map.symbols.middle", "@");
        this.radius = Math.max(3, plugin.getConfig().getInt("Map.chatRadius", 7));
        this.header = plugin.getLangManager().getRawString("Commands.LandMap.header");
        this.yours = plugin.getLangManager().getRawString("Commands.LandMap.yours");
        this.friends = plugin.getLangManager().getRawString("Commands.LandMap.friends");
        this.others = plugin.getLangManager().getRawString("Commands.LandMap.others");
        this.sendChatMap(p);
    }

    private static String getPlayerDirection(Player playerSelf) {
        String dir;
        float y = playerSelf.getLocation().getYaw();
        if (y < 0) {
            y += 360;
        }
        y %= 360;
        int i = (int) ((y + 8) / 22.5);
        if (i == 0) {
            dir = "south";
        } else if (i == 1) {
            dir = "south southwest";
        } else if (i == 2) {
            dir = "southwest";
        } else if (i == 3) {
            dir = "west southwest";
        } else if (i == 4) {
            dir = "west";
        } else if (i == 5) {
            dir = "west northwest";
        } else if (i == 6) {
            dir = "northwest";
        } else if (i == 7) {
            dir = "north northwest";
        } else if (i == 8) {
            dir = "north";
        } else if (i == 9) {
            dir = "north northeast";
        } else if (i == 10) {
            dir = "northeast";
        } else if (i == 11) {
            dir = "east northeast";
        } else if (i == 12) {
            dir = "east";
        } else if (i == 13) {
            dir = "east southeast";
        } else if (i == 14) {
            dir = "southeast";
        } else if (i == 15) {
            dir = "south southeast";
        } else {
            dir = "south";
        }
        return dir;
    }

    public Player getMapViewer() {
        return null;
    }

    void removeMap() {
    }

    private void sendChatMap(Player p) {
        Chunk center = p.getLocation().getChunk();
        Map<Chunk, IOwnedLand> nearby = wg.getNearbyLands(p.getLocation(), radius, radius);

        p.sendMessage(ChatColor.DARK_GRAY + "------------------------------");
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', header));

        for (int dz = -radius; dz <= radius; dz++) {
            TextComponent row = new TextComponent("");
            for (int dx = -radius; dx <= radius; dx++) {
                int chunkX = center.getX() + dx;
                int chunkZ = center.getZ() + dz;
                Chunk chunk = p.getWorld().getChunkAt(chunkX, chunkZ);

                String symbol = ((chunkX + chunkZ) & 1) == 0 ? background1 : background2;
                ChatColor color = ChatColor.GRAY;
                IOwnedLand land = nearby.get(chunk);

                if (dx == 0 && dz == 0) {
                    symbol = middleSymbol;
                    color = ChatColor.RESET;
                } else if (land != null) {
                    if (land.getOwner().equals(p.getUniqueId())) {
                        symbol = ownSymbol;
                        color = ChatColor.GREEN;
                    } else if (land.isFriend(p.getUniqueId())) {
                        symbol = friendsSymbol;
                        color = ChatColor.YELLOW;
                    } else {
                        symbol = foreignSymbol;
                        color = ChatColor.RED;
                    }
                }

                TextComponent cell = new TextComponent(color + symbol);
                cell.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(ChatColor.GOLD + "Chunk " + chunkX + ", " + chunkZ + "\n"
                                + ChatColor.GRAY + "Click para claimear").create()));
                cell.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/land map claim " + chunkX + " " + chunkZ));
                row.addExtra(cell);
            }
            p.spigot().sendMessage(row);
        }

        p.sendMessage(ChatColor.GREEN + ownSymbol + ChatColor.GRAY + " - " + ChatColor.translateAlternateColorCodes('&', yours)
                + ChatColor.DARK_GRAY + " | "
                + ChatColor.YELLOW + friendsSymbol + ChatColor.GRAY + " - " + ChatColor.translateAlternateColorCodes('&', friends)
                + ChatColor.DARK_GRAY + " | "
                + ChatColor.RED + foreignSymbol + ChatColor.GRAY + " - " + ChatColor.translateAlternateColorCodes('&', others));
        p.sendMessage(ChatColor.GRAY + "Click en un chunk para ejecutar /land claim en esa posicion.");
    }

    void forceUpdate() {
    }
}
