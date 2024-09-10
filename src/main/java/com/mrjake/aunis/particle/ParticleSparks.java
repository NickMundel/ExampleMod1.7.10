package com.mrjake.aunis.particle;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;

public class ParticleSparks extends EntityFX {

    public ParticleSparks(World world, double x, double y, double z, double motionX, double motionZ, boolean falling) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);

        this.motionX = motionX;
        this.motionZ = motionZ;
        this.motionY = 0;

        // Anpassen der Partikeleinstellungen
        this.particleMaxAge = 20; // Lebensdauer des Partikels in Ticks
        this.particleScale = 1.0F; // Größe des Partikels
        this.particleRed = 1.0F;
        this.particleGreen = 1.0F;
        this.particleBlue = 1.0F;
        this.particleAlpha = 1.0F;

        this.setParticleTextureIndex(0); // Texturindex des Partikels
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        // Bewegung des Partikels aktualisieren
        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        // Überprüfen, ob der Partikel abgelaufen ist
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
        }
    }
}
