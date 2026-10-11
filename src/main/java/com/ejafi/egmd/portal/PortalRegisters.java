package com.ejafi.egmd.portal;

import com.ejafi.egmd.EGMD;
import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PortalRegisters {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EGMD.MOD_ID);
    private static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, EGMD.MOD_ID);
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, EGMD.MOD_ID);

    public static final Map<PortalVariant, DeferredBlock<MiningPortalBlock>> PORTAL_BLOCKS =
            registerPerVariant(variant -> BLOCKS.register(
                    variant.id(), () -> new MiningPortalBlock(createPortalProperties(variant.mapColor()), variant)));

    private static final Map<PortalVariant, DeferredHolder<PoiType, PoiType>> PORTAL_POI_TYPES =
            registerPerVariant(variant -> POI_TYPES.register(variant.id(), () -> {
                MiningPortalBlock block = getPortalBlock(variant).get();
                return new PoiType(Set.copyOf(block.getStateDefinition().getPossibleStates()), 1, 1);
            }));

    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> PORTAL_SPARK =
            PARTICLE_TYPES.register("portal_spark", () -> new ParticleType<ColorParticleOption>(false) {
                @Override
                public MapCodec<ColorParticleOption> codec() {
                    return ColorParticleOption.codec(this);
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                    return ColorParticleOption.streamCodec(this);
                }
            });

    private PortalRegisters() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        POI_TYPES.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
    }

    public static DeferredBlock<MiningPortalBlock> getPortalBlock(PortalVariant variant) {
        return Objects.requireNonNull(PORTAL_BLOCKS.get(variant));
    }

    static ResourceKey<PoiType> getPoiTypeKey(PortalVariant variant) {
        return Objects.requireNonNull(PORTAL_POI_TYPES.get(variant)).getKey();
    }

    private static <H> Map<PortalVariant, H> registerPerVariant(Function<PortalVariant, H> registration) {
        Map<PortalVariant, H> holders = new LinkedHashMap<>();
        for (PortalVariant variant : PortalVariants.ALL) {
            holders.put(variant, registration.apply(variant));
        }
        return Collections.unmodifiableMap(holders);
    }

    private static BlockBehaviour.Properties createPortalProperties(MapColor color) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .noCollission()
                .strength(-1.0F)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 11)
                .noLootTable()
                .pushReaction(PushReaction.BLOCK);
    }
}
