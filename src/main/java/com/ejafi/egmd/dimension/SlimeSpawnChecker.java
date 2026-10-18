package com.ejafi.egmd.dimension;

import com.ejafi.egmd.Egmd;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

@EventBusSubscriber(modid = Egmd.MOD_ID)
public final class SlimeSpawnChecker {
    public static final TagKey<Biome> ALLOWS_FULL_MOON_SLIME_SPAWNS =
            TagKey.create(Registries.BIOME, Egmd.createLocation("allows_full_moon_slime_spawns"));
    static final int SURFACE_MIN_Y = 51;
    static final int SURFACE_MAX_Y = 69;
    static final float SPAWN_CHANCE = 0.5F;
    static final int DARKNESS_BOUND = 8;

    private SlimeSpawnChecker() {}

    @SubscribeEvent
    public static void onSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        ServerLevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!EntityType.SLIME.equals(event.getEntityType())
                || event.getDefaultResult()
                || MobSpawnType.isSpawner(event.getSpawnType())
                || level.getDifficulty() == Difficulty.PEACEFUL
                || !level.getBiome(pos).is(ALLOWS_FULL_MOON_SLIME_SPAWNS)) {
            return;
        }

        RandomSource random = event.getRandom();
        if (SurfaceRoll.roll(random).admits(pos.getY(), level.getMaxLocalRawBrightness(pos))
                && Mob.checkMobSpawnRules(EntityType.SLIME, level, event.getSpawnType(), pos, random)) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED);
        }
    }

    record SurfaceRoll(float chance, int darkness) {
        static SurfaceRoll roll(RandomSource random) {
            return new SurfaceRoll(random.nextFloat(), random.nextInt(DARKNESS_BOUND));
        }

        boolean admits(int y, int light) {
            return y >= SURFACE_MIN_Y && y <= SURFACE_MAX_Y && chance < SPAWN_CHANCE && light <= darkness;
        }
    }
}
