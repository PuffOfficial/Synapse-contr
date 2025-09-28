package com.m_w_k.synapse.api.connect;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@FunctionalInterface
public interface CapacityProvider {

    long getCapacity(long requested, @NotNull CompoundTag data, @Range(from = 0, to = Integer.MAX_VALUE) int ticksSinceLastCall, boolean simulate);
}
