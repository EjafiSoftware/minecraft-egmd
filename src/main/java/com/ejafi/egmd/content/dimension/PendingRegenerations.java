package com.ejafi.egmd.content.dimension;

import com.ejafi.egmd.EGMD;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Mining dimensions an operator has evacuated for regeneration. They stay closed until the server
 * stops, since regeneration finishes by deleting the dimension's folder while the server is down.
 */
@EventBusSubscriber(modid = EGMD.MODID)
public final class PendingRegenerations {
    private static final Set<ResourceKey<Level>> PENDING = ConcurrentHashMap.newKeySet();

    private PendingRegenerations() {}

    public static void mark(ResourceKey<Level> dimension) {
        PENDING.add(dimension);
    }

    public static boolean isPending(ResourceKey<Level> dimension) {
        return PENDING.contains(dimension);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        PENDING.clear();
    }
}
