package com.ejafi.egmd.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ColorParticleOption;
import org.jetbrains.annotations.NotNull;

/**
 * Vanilla portal particle recolored per portal variant, slightly smaller and translucent.
 */
public class PortalSparkParticle extends PortalParticle {
    private static final float SIZE_SCALE = 1F;

    private PortalSparkParticle(ClientLevel level, double x, double y, double z,
                                double xSpeed, double ySpeed, double zSpeed, ColorParticleOption color) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        float shade = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = color.getRed() * shade;
        this.gCol = color.getGreen() * shade;
        this.bCol = color.getBlue() * shade;
        this.quadSize *= SIZE_SCALE;
        this.setAlpha(color.getAlpha());
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(@NotNull ColorParticleOption color, @NotNull ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            PortalSparkParticle particle = new PortalSparkParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, color);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
