package com.ejafi.egmd.content.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ejafi.egmd.core.registration.ModDimensions;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class MiningPortalBlockTest {

    @Test
    void resolveDestination_leadsToMiningDimension_whenTravelingFromOverworld() {
        assertEquals(ModDimensions.NETHER_MINING_LEVEL_KEY,
                MiningPortalBlock.resolveDestination(Level.OVERWORLD, ModDimensions.NETHER_MINING_LEVEL_KEY));
    }

    @Test
    void resolveDestination_leadsToOverworld_whenTravelingFromPortalsOwnMiningDimension() {
        assertEquals(Level.OVERWORLD,
                MiningPortalBlock.resolveDestination(ModDimensions.NETHER_MINING_LEVEL_KEY, ModDimensions.NETHER_MINING_LEVEL_KEY));
    }

    @Test
    void resolveDestination_leadsNowhere_whenPortalStandsInTheNether() {
        assertNull(MiningPortalBlock.resolveDestination(Level.NETHER, ModDimensions.NETHER_MINING_LEVEL_KEY));
    }

    @Test
    void resolveDestination_leadsNowhere_whenPortalStandsInAnotherMiningDimension() {
        assertNull(MiningPortalBlock.resolveDestination(ModDimensions.END_MINING_LEVEL_KEY, ModDimensions.NETHER_MINING_LEVEL_KEY));
    }
}
