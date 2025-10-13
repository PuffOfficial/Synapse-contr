package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.block.ruleset.TransferRuleset;
import com.m_w_k.synapse.api.connect.AxonAddress;
import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.block.IFacedAxonBlockEntity;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public abstract class AbstractExposer<T, V extends AbstractExposer<T, V, G>, G> implements IEndpointCapability {
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

    @Override
    public abstract @Nullable TransferRuleset.QueryableRuleset<G> getRuleset();

    @Override
    public @NotNull V child(Direction facing) {
        return associated.map(v -> v.getSecond().child(facing), m -> m.computeIfAbsent(facing, this::constructChild));
    }

    protected abstract AxonType getType();

    protected Capability<T> getCapability() {
        return (Capability<T>) getType().getCapability();
    }

    public @NotNull IFacedAxonBlockEntity getOwner() {
        return owner;
    }

    protected boolean check(G g, AxonTree.Connection<T> connection, boolean incoming) {
        if (getRuleset() == null) return true;
        AxonAddress address = getRuleset().getMatchingAddress(g, incoming);
        if (address == null) return false;
        return connection.destination().matches(address);
    }

    protected @NotNull List<AxonTree.Connection<T>> getConnections() {
        if (associated.left().isEmpty()) return List.of();
        if (!getOwner().isRemoved() && getOwner().getLevel() != null) {
            long time = getOwner().getLevel().getGameTime();
            if (time != cacheTick || cache == null) {
                cache = AxonTree.load(getOwner().getLevel(), getType(), getCapability())
                        .map(t -> {
                            Collection<AxonAddress> seek;
                            if (getRuleset() != null) {
                                seek = getRuleset().getAllPossibleAddresses();
                            } else {
                                seek = List.of(AxonAddress.wildcard(true));
                            }
                            return t.find(getOwner().getByFace(associated.left().get().getFirst(), getType()).treeID(), seek);
                        })
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

    @Override
    public CompoundTag serializeNBT() {
        return associated.map(left -> left.getSecond().serializeNBT(), right -> {
            CompoundTag tag = new CompoundTag();
            tag.put("null", this.save());
            right.forEach((dir, child) -> {
                tag.put(dir.getName(), child.save());
            });
            return tag;
        });
    }

    protected abstract @NotNull Tag save();

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        associated.ifLeft(left -> left.getSecond().deserializeNBT(nbt))
                .ifRight(right -> {
                    this.load(nbt.getCompound("null"));
                    for (Direction dir : Direction.values()) {
                        Tag load = nbt.get(dir.getName());
                        if (load != null) {
                            child(dir).load(load);
                        }
                    }
                });
    }

    protected abstract void load(@NotNull Tag nbt);

    protected void setDirty() {
        owner.setChanged();
    }
}
