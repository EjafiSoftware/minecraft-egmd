package com.ejafi.egmd.dimension;

import com.ejafi.egmd.EGMD;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = EGMD.MOD_ID)
public final class HostileMobGuard {
    private HostileMobGuard() {}

    public static boolean admits(Entity entity, Set<Restriction> restrictions) {
        if (!restrictions.contains(Restriction.NO_HOSTILE_MOBS)) {
            return true;
        }

        return entity.getPassengersAndSelf().noneMatch(HostileMobGuard::isRefused);
    }

    private static boolean isRefused(Entity entity) {
        return entity instanceof Enemy && !(entity instanceof WitherBoss);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (!level.isClientSide() && !admits(event.getEntity(), EgmdDimensions.getRestrictions(level.dimension()))) {
            event.setCanceled(true);
        }
    }
}
