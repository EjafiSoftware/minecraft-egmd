package com.ejafi.egmd.dimension;

import com.ejafi.egmd.EGMD;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = EGMD.MOD_ID)
public final class HostileMobGuard {
    private HostileMobGuard() {}

    public static boolean admits(Entity entity, ResourceKey<Level> dimension) {
        if (MiningDimension.findByLevelKey(dimension).isEmpty()) {
            return true;
        }

        return entity.getPassengersAndSelf().noneMatch(Enemy.class::isInstance);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (!level.isClientSide() && !admits(event.getEntity(), level.dimension())) {
            event.setCanceled(true);
        }
    }
}
