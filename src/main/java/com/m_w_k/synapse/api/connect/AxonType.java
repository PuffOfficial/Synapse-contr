package com.m_w_k.synapse.api.connect;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.IExtensibleEnum;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

public enum AxonType implements StringRepresentable, IExtensibleEnum {
    ENERGY((r, d, t, sim) -> {
        long capacity = d.getLong("Capacity");
        long consumed = d.getLong("Consumed");
        if (t > 0) {
            capacity += (int) (consumed * 0.1f);
            d.remove("Consumed");
            consumed = 0;
            capacity = (int) (capacity * Math.pow(0.95f, t));
            if (!sim) d.putLong("Capacity", capacity);
        }
        if (!sim) d.putLong("Consumed", Math.min(r + consumed, capacity));
        capacity += r / 10;
        return capacity - consumed;
    }, ForgeCapabilities.ENERGY),
    ITEM((r, d, t, sim) -> {
        int baseStackCap = 1;
        long capacity = d.getInt("Capacity");
        long consumed = d.getInt("Consumed");
        if (t > 0) {
            int refreshInterval = 10;
            int timeSum = t + d.getInt("TimeSum");
            d.putInt("TimeSum", timeSum % refreshInterval);
            t = t / refreshInterval;
            if (t > 0) {
                long gap = capacity - consumed;
                capacity += gap;
                d.remove("Consumed");
                consumed = 0;
                if (t > 1) {
                    capacity = Math.max(baseStackCap * 64, capacity - baseStackCap * 64L * (t - 1));
                    if (!sim) d.putLong("Capacity", capacity);
                }
            }
        }
        if (!sim) d.putLong("Consumed", Math.min(r + consumed, capacity));
        return capacity - consumed;
    }, ForgeCapabilities.ITEM_HANDLER),
    FLUID((r, d, t, sim) -> {
        long baseCap = 1000;
        long capacity = d.getInt("Capacity");
        int consumed = d.getInt("Consumed");
        if (t > 0) {
            int refreshInterval = 10;
            int timeSum = t + d.getInt("TimeSum");
            d.putInt("TimeSum", timeSum % refreshInterval);
            t = t / refreshInterval;
            if (t > 0) {
                long gap = capacity - consumed;
                capacity += gap;
                d.remove("Consumed");
                consumed = 0;
                if (t > 1) {
                    capacity = Math.max(baseCap, capacity - baseCap * (t - 1));
                    if (!sim) d.putLong("Capacity", capacity);
                }
            }
        }
        if (!sim) d.putLong("Consumed", Math.min(r + consumed, capacity));
        return capacity - consumed;
    }, ForgeCapabilities.FLUID_HANDLER);

    public static final Codec<AxonType> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(AxonType::values, AxonType::valueOf);

    private final @NotNull CapacityProvider provider;
    private final @NotNull Capability<?> capability;

    AxonType(@NotNull CapacityProvider provider, @NotNull Capability<?> capability) {
        this.provider = provider;
        this.capability = capability;
    }

    public static AxonType create(String name, CapacityProvider provider, @NotNull Capability<?> capability) {
        throw new IllegalStateException("Enum not extended");
    }

    public @NotNull CapacityProvider getProvider() {
        return provider;
    }

    public @NotNull Capability<?> getCapability() {
        return capability;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name();
    }
}
