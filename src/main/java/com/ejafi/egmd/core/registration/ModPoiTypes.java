package com.ejafi.egmd.core.registration;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.content.block.MiningPortalBlock;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ModPoiTypes {
    private ModPoiTypes() {
        /* This utility class should not be instantiated */
    }

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, EGMD.MODID);

    /**
     * package-private so ModBlocks lambdas can look up the holder by variant
     */
    static final Map<PortalVariant, DeferredHolder<PoiType, PoiType>> PORTAL_POI_TYPES = new LinkedHashMap<>();

    static {
        for (PortalVariant v : PortalVariants.ALL) {
            PORTAL_POI_TYPES.put(v, POI_TYPES.register(v.id(), () -> new PoiType(
                    Set.of(
                            ModBlocks.PORTAL_BLOCKS.get(v).get().defaultBlockState().setValue(MiningPortalBlock.AXIS, Axis.X),
                            ModBlocks.PORTAL_BLOCKS.get(v).get().defaultBlockState().setValue(MiningPortalBlock.AXIS, Axis.Z)
                    ), 1, 1)));
        }
    }

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
    }
}
