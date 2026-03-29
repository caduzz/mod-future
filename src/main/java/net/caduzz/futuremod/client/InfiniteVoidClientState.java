package net.caduzz.futuremod.client;

/** Estado local de HUD do infinite_void_domain no cliente. */
public final class InfiniteVoidClientState {

    private static int activeTicks;
    private static int cooldownTicks;

    private InfiniteVoidClientState() {
    }

    public static void sync(int active, int cooldown) {
        activeTicks = Math.max(0, active);
        cooldownTicks = Math.max(0, cooldown);
    }

    public static void tick() {
        if (activeTicks > 0) activeTicks--;
        if (cooldownTicks > 0) cooldownTicks--;
    }

    public static boolean isActive() {
        return activeTicks > 0;
    }

    public static boolean isOnCooldown() {
        return cooldownTicks > 0;
    }

    public static int activeSeconds() {
        return (int) Math.ceil(activeTicks / 20.0);
    }

    public static int cooldownSeconds() {
        return (int) Math.ceil(cooldownTicks / 20.0);
    }
}
