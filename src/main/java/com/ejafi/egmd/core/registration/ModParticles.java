package com.ejafi.egmd.core.registration;

import com.ejafi.egmd.EGMD;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

/**
 * Handles particle type registration for EGMD.
 */
public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, EGMD.MODID);

    /**
     * Portal ambience particle with the vanilla portal swoop, colored per portal variant.
     */
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> PORTAL_SPARK =
            PARTICLE_TYPES.register("portal_spark", () -> new ParticleType<ColorParticleOption>(false) {
                @Override
                public @NotNull MapCodec<ColorParticleOption> codec() {
                    return ColorParticleOption.codec(this);
                }

                @Override
                public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                    return ColorParticleOption.streamCodec(this);
                }
            });

    private ModParticles() {}

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
