package net.caduzz.futuremod.purplevoid;

/** Purple bolt after fusion: brief pop, then linear flight. */
public enum PurpleVoidPhase {
    FUSION_POP((byte) 0),
    BOLT((byte) 1);

    private final byte id;

    PurpleVoidPhase(byte id) {
        this.id = id;
    }

    public byte id() {
        return id;
    }

    public static PurpleVoidPhase fromId(byte id) {
        for (PurpleVoidPhase p : values()) {
            if (p.id == id) {
                return p;
            }
        }
        return BOLT;
    }
}
