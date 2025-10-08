package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.connect.AxonAddress;
import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.block.IFacedAxonBlockEntity;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;

public abstract class AbstractExposer<T, V extends AbstractExposer<T, V>> {
    protected final @NotNull IFacedAxonBlockEntity owner;

    protected @Nullable List<AxonTree.Connection<T>> cache;
    protected long cacheTick;

    protected final @NotNull Either<Pair<Direction, V>, EnumMap<Direction, V>> associated;

    protected AbstractExposer(@NotNull IFacedAxonBlockEntity owner) {
        this.owner = owner;
        associated = Either.right(new EnumMap<>(Direction.class));
    }

    protected AbstractExposer(@NotNull Direction dir, @NotNull V parent) {
        this.owner = parent.owner;
        associated = Either.left(Pair.of(dir, parent));
    }

    protected abstract V constructChild(Direction dir);

    public V child(Direction facing) {
        return associated.map(v -> v.getSecond().child(facing), m -> m.computeIfAbsent(facing, this::constructChild));
    }

    public static <V extends AbstractExposer<?, ?>, X> LazyOptional<X> sided(Direction facing, LazyOptional<V> parent) {
        return parent.lazyMap(p -> p.child(facing)).cast();
    }

    protected abstract AxonType getType();

    protected Capability<T> getCapability() {
        return (Capability<T>) getType().getCapability();
    }

    public @NotNull IFacedAxonBlockEntity getOwner() {
        return owner;
    }

    protected @NotNull List<AxonTree.Connection<T>> getConnections() {
        if (associated.left().isEmpty()) return List.of();
        if (!getOwner().isRemoved() && getOwner().getLevel() != null) {
            long time = getOwner().getLevel().getGameTime();
            if (time != cacheTick || cache == null) {
                cache = AxonTree.load(getOwner().getLevel(), getType(), getCapability())
                        .map(t -> t.find(getOwner().getByFace(associated.left().get().getFirst(), getType()).treeID(), AxonAddress.wildcard(true)))
                        .orElse(List.of());
                if (!cache.isEmpty() && cache.get(0).connection().isEmpty()) {
                    cache.remove(0);
                }
                cacheTick = time;
            }
            return cache;
        }
        return List.of();
    }

    protected long getAllowed(long desired, AxonTree.Connection<T> connection) {
        if (getOwner().getLevel() == null) return 0;
        for (AxonConnection connect : connection.connection()) {
            desired = connect.getCapacity(getOwner().getLevel(), desired, true);
        }
        return desired;
    }

    protected void consumeCapacity(long consumed, AxonTree.Connection<T> connection) {
        if (getOwner().getLevel() == null) return;
        for (AxonConnection connect : connection.connection()) {
            connect.getCapacity(getOwner().getLevel(), consumed, false);
        }
    }
}
