package net.caduzz.futuremod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Reversal (vermelho): aparece à direita do jogador e converge ao centro. */
public class RedVoidOrbEntity extends VoidSatelliteBase {

    public RedVoidOrbEntity(EntityType<? extends RedVoidOrbEntity> type, Level level) {
        super(type, level);
    }

    public RedVoidOrbEntity(EntityType<? extends RedVoidOrbEntity> type, Level level, LivingEntity owner, Vec3 spawnPos) {
        super(type, level, owner, spawnPos);
    }

    @Override
    protected boolean initiatesMerge() {
        return false;
    }
}
