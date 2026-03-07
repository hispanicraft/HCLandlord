package biz.princeps.landlord.api;

import java.util.Set;
import java.util.UUID;

public interface IClanAccessProvider {

    boolean isAvailable();

    boolean arePlayersInSameClan(UUID first, UUID second);

    Set<UUID> getClanMembersOfOwner(UUID owner);
}
