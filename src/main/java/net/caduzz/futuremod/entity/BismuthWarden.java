package net.caduzz.futuremod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.level.Level;

/**
 * Mob mais forte que o Warden, usa o mesmo modelo e estilo de combate.
 * Dropa peças da armadura de bismuto.
 */
public class BismuthWarden extends Warden {

    public BismuthWarden(EntityType<? extends Warden> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Warden.createAttributes()
            .add(Attributes.MAX_HEALTH, 650.0)
            .add(Attributes.ATTACK_DAMAGE, 42.0)
            .add(Attributes.MOVEMENT_SPEED, 0.35)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
            .add(Attributes.FOLLOW_RANGE, 50.0);
    }
}
