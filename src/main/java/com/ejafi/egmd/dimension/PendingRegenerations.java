package com.ejafi.egmd.dimension;

import com.ejafi.egmd.EGMD;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = EGMD.MOD_ID)
public final class PendingRegenerations {
    private static final Set<ResourceKey<Level>> PENDING = ConcurrentHashMap.newKeySet();

    private PendingRegenerations() {}

    public static void mark(ResourceKey<Level> dimension) {
        PENDING.add(dimension);
    }

    public static boolean isPending(ResourceKey<Level> dimension) {
        return PENDING.contains(dimension);
    }

    public static void clear() {
        PENDING.clear();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        clear();
    }
}
