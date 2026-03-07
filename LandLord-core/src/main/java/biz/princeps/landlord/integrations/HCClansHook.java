package biz.princeps.landlord.integrations;

import biz.princeps.landlord.api.IClanAccessProvider;
import biz.princeps.landlord.api.ILandLord;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class HCClansHook implements IClanAccessProvider {

    private final ILandLord plugin;
    private final Cache<UUID, Set<UUID>> membersCache;

    private Plugin delegate;
    private Method sameClanMethod;
    private Method membersMethod;
    private boolean warnedMissing;
    private boolean warnedContract;

    public HCClansHook(ILandLord plugin) {
        this.plugin = plugin;
        this.membersCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean isAvailable() {
        return resolve();
    }

    @Override
    public boolean arePlayersInSameClan(UUID first, UUID second) {
        if (first == null || second == null) {
            return false;
        }
        if (first.equals(second)) {
            return true;
        }
        if (!resolve()) {
            return false;
        }

        try {
            Object result = sameClanMethod.invoke(delegate, first, second);
            return result instanceof Boolean && (Boolean) result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            clearResolvedState();
            plugin.getLogger().warning("HCClans hook failed while checking clan membership: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Set<UUID> getClanMembersOfOwner(UUID owner) {
        if (owner == null || !resolve()) {
            return Collections.emptySet();
        }

        try {
            return membersCache.get(owner, new java.util.concurrent.Callable<Set<UUID>>() {
                @Override
                public Set<UUID> call() {
                    return loadClanMembers(owner);
                }
            });
        } catch (ExecutionException e) {
            plugin.getLogger().warning("HCClans hook failed while loading clan members for " + owner + ": " + e.getMessage());
            return Collections.emptySet();
        }
    }

    private Set<UUID> loadClanMembers(UUID owner) {
        try {
            Object result = membersMethod.invoke(delegate, owner);
            if (!(result instanceof Set)) {
                return Collections.emptySet();
            }

            Set<?> rawMembers = (Set<?>) result;
            Set<UUID> members = new HashSet<>();
            for (Object rawMember : rawMembers) {
                if (rawMember instanceof UUID) {
                    members.add((UUID) rawMember);
                }
            }
            return Collections.unmodifiableSet(members);
        } catch (IllegalAccessException | InvocationTargetException e) {
            clearResolvedState();
            plugin.getLogger().warning("HCClans hook failed while loading clan members: " + e.getMessage());
            return Collections.emptySet();
        }
    }

    private boolean resolve() {
        Plugin current = plugin.getServer().getPluginManager().getPlugin("HCClans");
        if (current == null || !current.isEnabled()) {
            clearResolvedState();
            if (!warnedMissing) {
                warnedMissing = true;
                plugin.getLogger().info("HCClans not found or not enabled. Clan access in LandLord will stay disabled.");
            }
            return false;
        }

        if (delegate == current && sameClanMethod != null && membersMethod != null) {
            return true;
        }

        try {
            Method newSameClanMethod = current.getClass().getMethod("arePlayersInSameClan", UUID.class, UUID.class);
            Method newMembersMethod = current.getClass().getMethod("getClanMembersOfOwner", UUID.class);

            if (!Boolean.TYPE.equals(newSameClanMethod.getReturnType()) && !Boolean.class.equals(newSameClanMethod.getReturnType())) {
                throw new IllegalStateException("arePlayersInSameClan must return boolean");
            }
            if (!Set.class.isAssignableFrom(newMembersMethod.getReturnType())) {
                throw new IllegalStateException("getClanMembersOfOwner must return Set");
            }

            delegate = current;
            sameClanMethod = newSameClanMethod;
            membersMethod = newMembersMethod;
            warnedMissing = false;
            warnedContract = false;
            membersCache.invalidateAll();
            return true;
        } catch (NoSuchMethodException | IllegalStateException e) {
            clearResolvedState();
            if (!warnedContract) {
                warnedContract = true;
                plugin.getLogger().warning("HCClans is present but does not match the expected access contract: " + e.getMessage());
            }
            return false;
        }
    }

    private void clearResolvedState() {
        delegate = null;
        sameClanMethod = null;
        membersMethod = null;
        membersCache.invalidateAll();
    }
}
