package com.ejafi.egmd.core.registration;

import com.ejafi.egmd.EGMD;
import com.ejafi.egmd.content.block.MiningPortalBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

public class ModPoiTypes {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, EGMD.MODID);

    public static final DeferredHolder<PoiType, PoiType> MINING_PORTAL =
            POI_TYPES.register("mining_portal", () -> new PoiType(
                    Set.of(
                            ModBlocks.MINING_PORTAL.get().defaultBlockState().setValue(MiningPortalBlock.AXIS, Direction.Axis.X),
                            ModBlocks.MINING_PORTAL.get().defaultBlockState().setValue(MiningPortalBlock.AXIS, Direction.Axis.Z)
                    ), 1, 1));

    public static final DeferredHolder<PoiType, PoiType> NETHER_MINING_PORTAL =
            POI_TYPES.register("nether_mining_portal", () -> new PoiType(
                    Set.of(
                            ModBlocks.NETHER_MINING_PORTAL.get().defaultBlockState().setValue(MiningPortalBlock.AXIS, Direction.Axis.X),
                            ModBlocks.NETHER_MINING_PORTAL.get().defaultBlockState().setValue(MiningPortalBlock.AXIS, Direction.Axis.Z)
                    ), 1, 1));

    public static final DeferredHolder<PoiType, PoiType> END_MINING_PORTAL =
            POI_TYPES.register("end_mining_portal", () -> new PoiType(
                    Set.of(
                            ModBlocks.END_MINING_PORTAL.get().defaultBlockState().setValue(MiningPortalBlock.AXIS, Direction.Axis.X),
                            ModBlocks.END_MINING_PORTAL.get().defaultBlockState().setValue(MiningPortalBlock.AXIS, Direction.Axis.Z)
                    ), 1, 1));

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
    }
}
