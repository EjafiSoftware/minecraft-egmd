package com.ejafi.egmd.content.dimension;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.core.registration.ModDimensions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Keeps hostile mobs out of the mining dimensions, however they would arrive: spawned, summoned,
 * carried through a portal, or loaded from an older save.
 */
@EventBusSubscriber(modid = EGMD.MODID)
public final class HostileMobBan {
    private HostileMobBan() {}

    /**
     * Returns {@code false} if {@code entity}, or anything riding it, is a hostile mob and
     * {@code dimension} is a mining dimension.
     */
    public static boolean admits(Entity entity, ResourceKey<Level> dimension) {
        if (!ModDimensions.ALL_MINING_DIMENSIONS.contains(dimension)) return true;
        return entity.getPassengersAndSelf().noneMatch(Enemy.class::isInstance);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && !admits(event.getEntity(), event.getLevel().dimension())) {
            event.setCanceled(true);
        }
    }
}
