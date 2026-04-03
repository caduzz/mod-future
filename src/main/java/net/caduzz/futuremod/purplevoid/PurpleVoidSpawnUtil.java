package net.caduzz.futuremod.purplevoid;

import net.caduzz.futuremod.Config;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class PurpleVoidSpawnUtil {

    private PurpleVoidSpawnUtil() {
    }

    /** Unit vector perpendicular to horizontal look (right side of player in XZ). */
    public static Vec3 horizontalRight(LivingEntity entity) {
        Vec3 look = entity.getLookAngle();
        double lx = look.x;
        double lz = look.z;
        double h = Math.sqrt(lx * lx + lz * lz);
        if (h < 1e-4) {
            return new Vec3(1.0, 0.0, 0.0);
        }
        return new Vec3(-lz / h, 0.0, lx / h);
    }

    /**
     * Where the orbs converge: along the player's view from the eyes, so the whole effect sits
     * in front of the crosshair (visible in 1st person, not hidden inside the model).
     */
    public static Vec3 fusionTarget(LivingEntity owner) {
        Vec3 eye = owner.getEyePosition(1.0f);
        Vec3 look = owner.getLookAngle();
        double len = look.length();
        if (len < 1e-5) {
            look = new Vec3(0.0, -1.0, 0.0);
        } else {
            look = look.scale(1.0 / len);
        }
        double forward = Config.PURPLE_VOID_ANCHOR_FORWARD_ALONG_LOOK.get();
        return eye.add(look.scale(forward));
    }

    /** Fixed per-tick step toward {@link #fusionTarget}; independent of frame rate. */
    public static void stepTowardFusionCenter(Entity orb, LivingEntity owner) {
        Vec3 target = fusionTarget(owner);
        Vec3 cur = orb.position();
        Vec3 to = target.subtract(cur);
        double dist = to.length();
        if (dist < 1e-5) {
            return;
        }
        double maxStep = Config.PURPLE_VOID_APPROACH_BLOCKS_PER_TICK.get();
        double step = Math.min(dist, maxStep);
        Vec3 move = to.scale(step / dist);
        orb.setPos(cur.add(move));
    }
}
