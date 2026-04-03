package net.caduzz.futuremod.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * Resolves void-tech owners only when they live in the same {@link Level} as the entity.
 * After a dimension change, {@code ownerCached} would still point at the player while orbs
 * stayed behind — fusion targets used wrong-world coordinates and clients could desync.
 */
public final class VoidOwnerResolver {

    private VoidOwnerResolver() {
    }

    @Nullable
    public static LivingEntity resolve(Entity self, @Nullable UUID ownerUuid, @Nullable LivingEntity cached) {
        Level world = self.level();
        if (!(world instanceof ServerLevel sl) || ownerUuid == null) {
            return null;
        }
        if (cached != null) {
            if (!cached.isAlive() || cached.level() != world) {
                cached = null;
            } else {
                return cached;
            }
        }
        Entity found = sl.getEntity(ownerUuid);
        if (found instanceof LivingEntity le && le.isAlive()) {
            return le;
        }
        return null;
    }
}
