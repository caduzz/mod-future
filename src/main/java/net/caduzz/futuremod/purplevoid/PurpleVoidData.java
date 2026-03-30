package net.caduzz.futuremod.purplevoid;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** Cooldown do Purple Void no jogador (servidor). */
public final class PurpleVoidData implements INBTSerializable<CompoundTag> {

    private int cooldownTicks;

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public boolean canActivate() {
        return cooldownTicks <= 0;
    }

    public void setCooldownTicks(int ticks) {
        this.cooldownTicks = Math.max(0, ticks);
    }

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("cooldown_ticks", cooldownTicks);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        cooldownTicks = Math.max(0, tag.getInt("cooldown_ticks"));
    }
}
