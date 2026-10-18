package com.ejafi.egmd.dimension;

import com.ejafi.egmd.Egmd;
import com.ejafi.egmd.GameTestTemplates;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.vehicle.Boat;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Egmd.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HostileMobGuardGameTests {
    private static final BlockPos SPAWN = new BlockPos(3, 1, 3);

    private HostileMobGuardGameTests() {}

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void admits_refuses_zombie_when_destination_forbids_hostile_mobs(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, SPAWN);

        boolean admitted =
                HostileMobGuard.admits(zombie, RestrictionsFaker.generateIncluding(Restriction.NO_HOSTILE_MOBS));

        helper.assertValueEqual(admitted, false, "zombie admitted");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void admits_refuses_boat_when_it_carries_a_zombie_into_dimension_forbidding_hostile_mobs(
            GameTestHelper helper) {
        Boat boat = helper.spawn(EntityType.BOAT, SPAWN);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, SPAWN);
        zombie.startRiding(boat, true);

        boolean admitted =
                HostileMobGuard.admits(boat, RestrictionsFaker.generateIncluding(Restriction.NO_HOSTILE_MOBS));

        helper.assertValueEqual(admitted, false, "boat carrying a zombie admitted");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void admits_accepts_cow_when_destination_forbids_hostile_mobs(GameTestHelper helper) {
        Cow cow = helper.spawn(EntityType.COW, SPAWN);

        boolean admitted =
                HostileMobGuard.admits(cow, RestrictionsFaker.generateIncluding(Restriction.NO_HOSTILE_MOBS));

        helper.assertValueEqual(admitted, true, "cow admitted");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void admits_accepts_wither_when_destination_forbids_hostile_mobs(GameTestHelper helper) {
        WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN);

        boolean admitted =
                HostileMobGuard.admits(wither, RestrictionsFaker.generateIncluding(Restriction.NO_HOSTILE_MOBS));

        helper.assertValueEqual(admitted, true, "wither admitted");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void admits_refuses_wither_when_it_carries_a_zombie_into_dimension_forbidding_hostile_mobs(
            GameTestHelper helper) {
        WitherBoss wither = helper.spawn(EntityType.WITHER, SPAWN);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, SPAWN);
        zombie.startRiding(wither, true);

        boolean admitted =
                HostileMobGuard.admits(wither, RestrictionsFaker.generateIncluding(Restriction.NO_HOSTILE_MOBS));

        helper.assertValueEqual(admitted, false, "wither carrying a zombie admitted");
        helper.succeed();
    }

    @GameTest(template = GameTestTemplates.EMPTY_7X7X7)
    public static void admits_accepts_zombie_when_destination_allows_hostile_mobs(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, SPAWN);

        boolean admitted =
                HostileMobGuard.admits(zombie, RestrictionsFaker.generateExcluding(Restriction.NO_HOSTILE_MOBS));

        helper.assertValueEqual(admitted, true, "zombie admitted");
        helper.succeed();
    }
}
