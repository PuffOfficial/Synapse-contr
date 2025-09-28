package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record LocalConnectorDevice(@NotNull Map<AxonType, LocalAxonConnection> upstream, @NotNull ConnectorLevel level,
                                   @NotNull UUID treeID) {
    private static final Codec<Map<AxonType, LocalAxonConnection>> UPSTREAM_CODEC = Codec.unboundedMap(AxonType.CODEC, LocalAxonConnection.CODEC);

    public static final Codec<LocalConnectorDevice> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    UPSTREAM_CODEC.fieldOf("upstream").forGetter(LocalConnectorDevice::upstream),
                    ConnectorLevel.CODEC.fieldOf("tier").forGetter(LocalConnectorDevice::level),
                    UUIDUtil.CODEC.fieldOf("treeID").forGetter(LocalConnectorDevice::treeID)
            ).apply(instance, LocalConnectorDevice::new));

    public LocalConnectorDevice(@NotNull ConnectorLevel level) {
        this(new Reference2ObjectOpenHashMap<>(), level, UUID.randomUUID());
    }

    public LocalConnectorDevice(@NotNull ConnectorLevel level, @NotNull UUID treeID) {
        this(new Reference2ObjectOpenHashMap<>(), level, treeID);
    }

    public void ensureRegistered(@NotNull LevelAccessor level, @NotNull AxonType type) {
        ensureRegistered(level, type, type.getCapability(), null);
    }

    public <T> void ensureRegistered(@NotNull LevelAccessor level, @NotNull AxonType type, @NotNull Capability<T> cap, @Nullable T instance) {
        AxonTree.load(level, type, cap).ifPresent(
                t -> t.register(treeID, (short) (Math.random() * Short.MAX_VALUE), level(), instance));
    }

    public boolean hasUpstream(@NotNull AxonType type) {
        return upstream.containsKey(type);
    }
}
