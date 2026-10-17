package com.ejafi.egmd.dimension;

import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record EgmdDimension(ResourceKey<Level> levelKey, Terrain terrain, Set<Restriction> restrictions) {
    public EgmdDimension {
        restrictions = Set.copyOf(restrictions);
    }
}
