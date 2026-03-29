package net.caduzz.futuremod.domain;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** Estado persistente de cooldown e duracao da habilidade infinite_void_domain. */
public class InfiniteVoidDomainData implements INBTSerializable<CompoundTag> {

    private int activeTicks;
    private int cooldownTicks;

    public int getActiveTicks() {
        return activeTicks;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public boolean isActive() {
        return activeTicks > 0;
    }

    public boolean canActivate() {
        return activeTicks <= 0 && cooldownTicks <= 0;
    }

    public void activate(int durationTicks, int cooldownTicks) {
        this.activeTicks = Math.max(1, durationTicks);
        this.cooldownTicks = Math.max(this.cooldownTicks, cooldownTicks);
    }

    public void tick() {
        if (activeTicks > 0) activeTicks--;
        if (cooldownTicks > 0) cooldownTicks--;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("active_ticks", activeTicks);
        tag.putInt("cooldown_ticks", cooldownTicks);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        activeTicks = Math.max(0, tag.getInt("active_ticks"));
        cooldownTicks = Math.max(0, tag.getInt("cooldown_ticks"));
    }
}
