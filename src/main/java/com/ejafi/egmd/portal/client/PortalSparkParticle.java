package com.ejafi.egmd.portal.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ColorParticleOption;

final class PortalSparkParticle extends PortalParticle {
    private PortalSparkParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            ColorParticleOption color) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        float shade = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = color.getRed() * shade;
        this.gCol = color.getGreen() * shade;
        this.bCol = color.getBlue() * shade;
        this.setAlpha(color.getAlpha());
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    static final class Provider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprite;

        Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(
                ColorParticleOption color,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed) {
            var particle = new PortalSparkParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, color);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
