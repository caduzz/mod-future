package net.caduzz.futuremod.client;

/** Estado de freeze vindo do servidor (dominio). */
public final class DomainFreezeClientState {

    private static boolean frozen;

    private DomainFreezeClientState() {
    }

    public static void setFrozen(boolean value) {
        frozen = value;
    }

    public static boolean isFrozen() {
        return frozen;
    }
}
