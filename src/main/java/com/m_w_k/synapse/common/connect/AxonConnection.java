package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Connection data owned by an axon tree
 */
public sealed class AxonConnection permits LocalAxonConnection {

    public static final Codec<AxonConnection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AxonType.CODEC.fieldOf("axonType").forGetter(AxonConnection::getAxonType),
                    CompoundTag.CODEC.fieldOf("data").forGetter(AxonConnection::getData),
                    Codec.INT.xmap(i -> ConnectionType.TYPES[i], ConnectionType::ordinal).fieldOf("connectionType").forGetter(AxonConnection::getConnectionType)
            ).apply(instance, AxonConnection::new));

    private final @NotNull AxonType axonType;
    private @NotNull CompoundTag data;

    private final @NotNull ConnectionType connectionType;

    public AxonConnection(@NotNull AxonType axonType) {
        this(axonType, null);
    }

    public AxonConnection(@NotNull AxonType axonType, @Nullable ConnectionType connectionType) {
        this(axonType, new CompoundTag(), connectionType);
    }

    protected AxonConnection(@NotNull AxonType axonType, CompoundTag tag, @Nullable ConnectionType connectionType) {
        this.axonType = axonType;
        this.data = tag;
        this.connectionType = connectionType == null ? ConnectionType.UNKNOWN : connectionType;
    }

    public long getCapacity(Level level, long requested, boolean simulate) {
        long tick = level.getGameTime();
        long prev = data.getLong("LastTick");
        long cap = axonType.getProvider().getCapacity(requested, data, (int) (tick - prev), simulate);
        if (!simulate) data.putLong("LastTick", tick);
        return cap;
    }

    protected @NotNull CompoundTag getData() {
        return data;
    }

    public @NotNull AxonType getAxonType() {
        return axonType;
    }

    public @NotNull ConnectionType getConnectionType() {
        return connectionType;
    }
}
