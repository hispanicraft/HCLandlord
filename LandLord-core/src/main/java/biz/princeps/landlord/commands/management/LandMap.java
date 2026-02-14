package biz.princeps.landlord.commands.management;

import biz.princeps.landlord.api.ILandLord;
import biz.princeps.landlord.api.Options;
import biz.princeps.landlord.commands.LandlordCommand;
import biz.princeps.landlord.commands.claiming.Claim;
import biz.princeps.lib.command.Arguments;
import biz.princeps.lib.command.Properties;
import com.google.common.collect.Sets;
import io.papermc.lib.PaperLib;
import org.bukkit.entity.Player;

/**
 * Project: LandLord
 * Created by Alex D. (SpatiumPrinceps)
 * Date: 19/7/17
 */
public class LandMap extends LandlordCommand {

    private final Claim claimCommand;

    public LandMap(ILandLord plugin) {
        super(plugin, plugin.getConfig().getString("CommandSettings.Map.name"),
                plugin.getConfig().getString("CommandSettings.Map.usage"),
                Sets.newHashSet(plugin.getConfig().getStringList("CommandSettings.Map.permissions")),
                Sets.newHashSet(plugin.getConfig().getStringList("CommandSettings.Map.aliases")));
        this.claimCommand = new Claim(plugin, false);
    }

    @Override
    public void onCommand(Properties properties, Arguments arguments) {
        if (!properties.isPlayer()) {
            return;
        }

        Player player = properties.getPlayer();

        if (arguments.size() == 0) {
            onToggleLandMap(player);
            return;
        }

        String arg = arguments.get(0);

        if (arguments.size() == 1) {
            if (arg.equalsIgnoreCase("on") || arg.equalsIgnoreCase("off")) {
                onToggleLandMap(player, arg.toLowerCase());
            }
            return;
        }

        if (arg.equalsIgnoreCase("claim") && arguments.size() == 3) {
            onMapClaim(player, arguments.get(1), arguments.get(2));
        }
    }

    private void onMapClaim(Player player, String chunkXRaw, String chunkZRaw) {
        if (isDisabledWorld(player)) {
            return;
        }

        if (!hasClaimPermission(player)) {
            lm.sendMessage(player, lm.getString(player, "noPermissions"));
            return;
        }

        int chunkX;
        int chunkZ;

        try {
            chunkX = Integer.parseInt(chunkXRaw);
            chunkZ = Integer.parseInt(chunkZRaw);
        } catch (NumberFormatException ex) {
            lm.sendMessage(player, lm.getString(player, "Commands.Manage.invalidArguments"));
            return;
        }

        PaperLib.getChunkAtAsync(player.getWorld(), chunkX, chunkZ).thenAccept(chunk -> {
            claimCommand.onClaim(player, chunk);
        });
    }

    private boolean hasClaimPermission(Player player) {
        for (String permission : plugin.getConfig().getStringList("CommandSettings.Claim.permissions")) {
            if (player.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    private void onToggleLandMap(Player player) {

        if (isDisabledWorld(player))
            return;

        if (Options.enabled_map())
            plugin.getMapManager().toggleMap(player);
        else {
            lm.sendMessage(player, lm.getString(player, "Commands.LandMap.disabled"));
        }

    }

    private void onToggleLandMap(Player player, String state) {
        if (isDisabledWorld(player))
            return;

        if (!Options.enabled_map()) {
            lm.sendMessage(player, lm.getString(player, "Commands.LandMap.disabled"));
            return;
        }

        if (state.equals("on")) {
            plugin.getMapManager().addMap(player);
        } else {
            plugin.getMapManager().removeMap(player);
        }
    }
}
