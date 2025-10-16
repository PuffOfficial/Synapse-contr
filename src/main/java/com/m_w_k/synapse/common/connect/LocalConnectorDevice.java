package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.connect.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class LocalConnectorDevice implements ConnectorLevel.Provider {
    public static final Codec<LocalConnectorDevice> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AxonType.CODEC.fieldOf("type").forGetter(LocalConnectorDevice::type),
                    ConnectorLevel.CODEC.fieldOf("level").forGetter(LocalConnectorDevice::getLevel),
                    UUIDUtil.CODEC.fieldOf("treeID").forGetter(LocalConnectorDevice::treeID),
                    LocalAxonConnection.CODEC.optionalFieldOf("upstream").forGetter(d -> Optional.ofNullable(d.upstream())),
                    DeviceDataKey.MAP_CODEC.fieldOf("data").forGetter(LocalConnectorDevice::getData)
            ).apply(instance, LocalConnectorDevice::new));
    private final @NotNull AxonType type;
    private final @NotNull ConnectorLevel level;
    private final @NotNull UUID treeID;

    private @Nullable LocalAxonConnection upstream;
    private @Nullable AxonTree<?>.ConnectorDevice cache;

    private final @NotNull Map<DeviceDataKey<?>, Object> data;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private LocalConnectorDevice(@NotNull AxonType type, @NotNull ConnectorLevel level, @NotNull UUID treeID,
                                 Optional<LocalAxonConnection> upstream, Map<DeviceDataKey<?>, Object> data) {
        this.type = type;
        this.level = level;
        this.treeID = treeID;
        this.upstream = upstream.orElse(null);
        this.data = data;
    }

    public LocalConnectorDevice(@NotNull AxonType type, @NotNull ConnectorLevel level, @NotNull UUID treeID) {
        this.type = type;
        this.level = level;
        this.treeID = treeID;
        this.data = new Object2ObjectOpenHashMap<>();
    }

    public LocalConnectorDevice(@NotNull AxonType type, @NotNull ConnectorLevel level) {
        this(type, level, UUID.randomUUID());
    }

    @Contract("_ -> this")
    public LocalConnectorDevice ensureRegistered(@NotNull LevelAccessor level) {
        return ensureRegistered(level, type.getCapability(), null);
    }

    @Contract("_, _, _ -> this")
    public <T> LocalConnectorDevice ensureRegistered(@NotNull LevelAccessor level, @NotNull Capability<T> cap, @Nullable T instance) {
        AxonTree.load(level, type, cap).ifPresent(
                t -> cache = t.register(treeID, randShort(), getLevel(), instance));
        return this;
    }

    private short randShort() {
        short rand = (short) (Math.random() * Short.MAX_VALUE);
        if (rand == AxonAddress.EMPTY) return 1;
        return rand;
    }

    public boolean needsRegistration(@NotNull AxonType type) {
        return cache() == null;
    }

    public @Nullable AxonTree<?>.ConnectorDevice cache() {
        return cache;
    }

    public short getAddressID() {
        if (cache() == null) return 0;
        return cache().getId();
    }

    public @NotNull IDSetResult setAddressID(short newID) {
        if (cache() == null) return IDSetResult.FAIL;
        return cache().setId(newID);
    }

    public @UnmodifiableView AxonAddress getAddress() {
        if (cache() == null) return null;
        return cache().getAddress();
    }

    public @NotNull AxonType type() {
        return type;
    }

    @Override
    public @NotNull ConnectorLevel getLevel() {
        return level;
    }

    @Override
    public @NotNull Map<DeviceDataKey<?>, Object> getData() {
        return data;
    }

    public @NotNull UUID treeID() {
        return treeID;
    }

    public @Nullable LocalAxonConnection upstream() {
        return upstream;
    }

    public @Nullable LocalAxonConnection setUpstream(@Nullable LocalAxonConnection upstream) {
        LocalAxonConnection ret = this.upstream;
        this.upstream = upstream;
        return ret;
    }
}
