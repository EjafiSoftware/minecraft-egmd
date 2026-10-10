package com.ejafi.egmd.content.dimension;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.core.registration.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * In-world tests for the hostile mob ban, which needs real entities to judge.
 */
@GameTestHolder(EGMD.MODID)
public class HostileMobBanGameTests {
    private static final String TEMPLATE = "empty_7x7x7";
    private static final BlockPos SPAWN = new BlockPos(3, 1, 3);

    private HostileMobBanGameTests() {}

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void admits_refuses_zombie_when_destination_is_mining_dimension(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, SPAWN);

        if (HostileMobBan.admits(zombie, ModDimensions.OVERWORLD_MINING_LEVEL_KEY)) {
            helper.fail("A zombie was admitted to a mining dimension");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void admits_refuses_boat_when_it_carries_a_zombie_into_mining_dimension(GameTestHelper helper) {
        Boat boat = helper.spawn(EntityType.BOAT, SPAWN);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, SPAWN);
        zombie.startRiding(boat, true);

        if (HostileMobBan.admits(boat, ModDimensions.OVERWORLD_MINING_LEVEL_KEY)) {
            helper.fail("A boat carrying a zombie was admitted to a mining dimension");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void admits_accepts_cow_when_destination_is_mining_dimension(GameTestHelper helper) {
        Cow cow = helper.spawn(EntityType.COW, SPAWN);

        if (!HostileMobBan.admits(cow, ModDimensions.OVERWORLD_MINING_LEVEL_KEY)) {
            helper.fail("A cow was refused entry to a mining dimension");
        }
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = TEMPLATE)
    public static void admits_accepts_zombie_when_destination_is_overworld(GameTestHelper helper) {
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, SPAWN);

        if (!HostileMobBan.admits(zombie, Level.OVERWORLD)) {
            helper.fail("A zombie was refused a return to the Overworld");
        }
        helper.succeed();
    }
}
