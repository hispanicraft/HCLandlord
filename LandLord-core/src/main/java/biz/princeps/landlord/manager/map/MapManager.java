package biz.princeps.landlord.manager.map;

import biz.princeps.landlord.api.ILandLord;
import biz.princeps.landlord.api.IMapManager;
import biz.princeps.landlord.util.MapConstants;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * File created by jcdesimp on 3/10/14. updated by SpatiumPrinceps on 19/07/17
 */
public class MapManager implements IMapManager {

    private final HashMap<UUID, LandMap> mapList;
    private final ILandLord plugin;
    private final MapConstants constants;

    public MapManager(ILandLord plugin) {
        super();
        this.plugin = plugin;
        this.mapList = new HashMap<>();
        this.constants = new MapConstants(plugin.getConfig());
    }

    @Override
    public void toggleMap(Player p) {
        addMap(p);
    }

    @Override
    public void addMap(Player player) {
        new LandMap(player, plugin, constants);
    }

    @Override
    public void removeMap(Player player) {
        // one-shot map mode: nothing to remove
    }

    @Override
    public void removeAllMaps() {
        for (UUID uuid : mapList.keySet()) {
            mapList.get(uuid).removeMap();
        }
        mapList.clear();
    }

    @Override
    public void updateAll() {
        // one-shot map mode: no active maps to refresh
    }

    @Override
    public void update(UUID playerUUID) {
        // one-shot map mode: no active map state per player
    }

    @Override
    public boolean hasMap(UUID playerUUID) {
        return mapList.containsKey(playerUUID);
    }
}
