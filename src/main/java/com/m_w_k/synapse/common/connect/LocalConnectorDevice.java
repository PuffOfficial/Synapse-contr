package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.connect.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class LocalConnectorDevice {
    public static final Codec<LocalConnectorDevice> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AxonType.CODEC.fieldOf("type").forGetter(LocalConnectorDevice::type),
                    ConnectorLevel.CODEC.fieldOf("level").forGetter(LocalConnectorDevice::level),
                    UUIDUtil.CODEC.fieldOf("treeID").forGetter(LocalConnectorDevice::treeID),
                    LocalAxonConnection.CODEC.optionalFieldOf("upstream").forGetter(d -> Optional.ofNullable(d.upstream()))
            ).apply(instance, LocalConnectorDevice::new));
    private final @NotNull AxonType type;
    private final @NotNull ConnectorLevel level;
    private final @NotNull UUID treeID;

    private @Nullable LocalAxonConnection upstream;
    private @Nullable AxonTree<?>.ConnectorData cache;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private LocalConnectorDevice(@NotNull AxonType type, @NotNull ConnectorLevel level, @NotNull UUID treeID,
                                 Optional<LocalAxonConnection> upstream) {
        this.type = type;
        this.level = level;
        this.treeID = treeID;
        this.upstream = upstream.orElse(null);
    }

    public LocalConnectorDevice(@NotNull AxonType type, @NotNull ConnectorLevel level, @NotNull UUID treeID) {
        this.type = type;
        this.level = level;
        this.treeID = treeID;
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
                t -> cache = t.register(treeID, randShort(), level(), instance));
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

    private @Nullable AxonTree<?>.ConnectorData cache() {
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

    public @NotNull ConnectorLevel level() {
        return level;
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
