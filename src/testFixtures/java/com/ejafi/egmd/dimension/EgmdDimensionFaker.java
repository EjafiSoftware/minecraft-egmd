package com.ejafi.egmd.dimension;

import com.ejafi.egmd.EGMD;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.datafaker.Faker;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public final class EgmdDimensionFaker {
    private static final Faker FAKER = new Faker();

    private final ResourceKey<Level> levelKey = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(EGMD.MOD_ID, FAKER.internet().uuid()));
    private final Terrain terrain = new FlatTerrainFaker().generate();
    private final Set<Restriction> restrictions = RestrictionsFaker.generate();

    public static EgmdDimension pickRegistered() {
        return FAKER.options().nextElement(EgmdDimensions.ALL);
    }

    public static EgmdDimension pickRegistered(Predicate<EgmdDimension> filter) {
        List<EgmdDimension> matching =
                EgmdDimensions.ALL.stream().filter(filter).toList();
        return FAKER.options().nextElement(matching);
    }

    public EgmdDimension generate() {
        return new EgmdDimension(levelKey, terrain, restrictions);
    }
}
