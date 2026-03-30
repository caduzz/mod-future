package net.caduzz.futuremod.client;

/** Cooldown do Purple Void sincronizado pelo servidor (HUD). */
public final class PurpleVoidClientState {

    private static int cooldownTicks;

    private PurpleVoidClientState() {
    }

    public static void sync(int cooldown) {
        cooldownTicks = Math.max(0, cooldown);
    }

    public static void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
    }

    public static boolean isOnCooldown() {
        return cooldownTicks > 0;
    }

    public static int cooldownSeconds() {
        return (int) Math.ceil(cooldownTicks / 20.0);
    }
}
