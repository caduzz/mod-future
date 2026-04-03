package net.caduzz.futuremod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Lapse (azul): aparece à esquerda do jogador e inicia a fusão com o vermelho. */
public class BlueVoidOrbEntity extends VoidSatelliteBase {

    private boolean chargedSpawn;

    public BlueVoidOrbEntity(EntityType<? extends BlueVoidOrbEntity> type, Level level) {
        super(type, level);
    }

    public BlueVoidOrbEntity(EntityType<? extends BlueVoidOrbEntity> type, Level level, LivingEntity owner, Vec3 spawnPos, boolean charged) {
        super(type, level, owner, spawnPos);
        this.chargedSpawn = charged;
    }

    public boolean isChargedSpawn() {
        return chargedSpawn;
    }

    @Override
    protected boolean initiatesMerge() {
        return true;
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("charged", chargedSpawn);
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        chargedSpawn = tag.getBoolean("charged");
    }
}
