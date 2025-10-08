package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.UnaryOperator;

public abstract class AxonBlockEntity extends BlockEntity implements IAxonBlockEntity {

    protected static final Codec<Collection<BlockPos>> DOWNSTREAM_CODEC = Codec.list(BlockPos.CODEC).xmap(UnaryOperator.identity(), ObjectArrayList::new);
    protected final @NotNull Set<BlockPos> downstream = new ObjectOpenHashSet<>();

    protected static final Codec<List<LocalConnectorDevice>> DEVICE_CODEC = Codec.list(LocalConnectorDevice.CODEC);
    protected final @NotNull List<LocalConnectorDevice> devices = new ObjectArrayList<>();

    public AxonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int getSlots() {
        return devices.size();
    }

    @Override
    public @NotNull LocalConnectorDevice getBySlot(int slot) {
        return devices.get(slot);
    }

    public @NotNull Vec3 renderOffsetForSlot(int slot) {
        return Vec3.ZERO;
    }

    public @Nullable LocalAxonConnection setUpstream(@NotNull LocalAxonConnection connection, boolean dropOld) {

        LocalAxonConnection prev = getBySlot(connection.getSourceSlot()).setUpstream(connection);
        if (getLevel() != null) {
            BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
            if (be instanceof AxonBlockEntity a) a.addDownstream(getBlockPos());
            if (prev != null) {
                be = getLevel().getBlockEntity(prev.getTargetPos());
                if (be instanceof AxonBlockEntity a) a.removeDownstream(getBlockPos());
                if (dropOld) {
                    Block.popResource(getLevel(), getBlockPos(), prev.getItem().getItemWhenRemoved(prev));
                }
            }
        }
        clientSyncDataChanged();
        return prev;
    }

    public @Nullable LocalAxonConnection removeUpstream(int slot, boolean drop) {
        LocalAxonConnection prev = getBySlot(slot).setUpstream(null);
        if (prev != null && getLevel() != null) {
            BlockEntity be = getLevel().getBlockEntity(prev.getTargetPos());
            if (be instanceof AxonBlockEntity a) a.removeDownstream(getBlockPos());
            if (drop) {
                Block.popResource(getLevel(), getBlockPos(), prev.getItem().getItemWhenRemoved(prev));
            }
            clientSyncDataChanged();
        }
        return prev;
    }

    public @NotNull @UnmodifiableView Set<BlockPos> getDownstream() {
        return downstream;
    }

    public boolean removeDownstream(@NotNull BlockPos pos) {
        boolean changed = downstream.remove(pos);
        if (changed) {
            setChanged();
        }
        return changed;
    }

    public boolean addDownstream(@NotNull BlockPos pos) {
        boolean changed = downstream.add(pos);
        if (changed) {
            setChanged();
        }
        return changed;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        DEVICE_CODEC.encodeStart(NbtOps.INSTANCE, devices)
                .get().ifLeft(t -> tag.put("Devices", t));
        return tag;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        DEVICE_CODEC.parse(NbtOps.INSTANCE, tag.get("Devices"))
                .get().ifLeft(c -> {
                    // prevent crashing issues due to corrupted NBT
                    if (c.size() != devices.size()) return;
                    devices.clear();
                    devices.addAll(c);
                });
        if (tag.contains("Downstream")) {
            downstream.clear();
            DOWNSTREAM_CODEC.parse(NbtOps.INSTANCE, tag.get("Downstream"))
                    .get().ifLeft(downstream::addAll);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        DEVICE_CODEC.encodeStart(NbtOps.INSTANCE, devices)
                .get().ifLeft(t -> tag.put("Devices", t));
        DOWNSTREAM_CODEC.encodeStart(NbtOps.INSTANCE, downstream)
                .get().ifLeft(t -> tag.put("Downstream", t));
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        // setRemoved() will be called immediately after this, prevent side effects by clearing our references
        downstream.clear();
        devices.clear();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!(getLevel() instanceof ServerLevel)) return;
        for (BlockPos pos : getDownstream()) {
            BlockEntity be = getLevel().getBlockEntity(pos);
            if (be instanceof AxonBlockEntity abe) {
                abe.onUpstreamRemoved();
            }
        }
        devices.forEach(d1 -> {
            AxonTree.load(getLevel(), d1.type(), d1.type().getCapability())
                    .ifPresent(t -> t.retire(d1.treeID()));
            LocalAxonConnection connection = d1.upstream();
            if (connection == null) return;
            BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
            if (be instanceof AxonBlockEntity a) {
                a.removeDownstream(getBlockPos());
            }
            Block.popResource(getLevel(), getBlockPos(), connection.getItem().getItemWhenRemoved(connection));
        });
    }

    public void onUpstreamRemoved() {
        if (getLevel() == null) return;
        devices.forEach(d -> {
            LocalAxonConnection connection = d.upstream();
            if (connection == null) return;
            BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
            if (!(be instanceof AxonBlockEntity) || be.isRemoved()) {
                Block.popResource(getLevel(), connection.getTargetPos(), connection.getItem().getItemWhenRemoved(connection));
                clientSyncDataChanged();
                d.setUpstream(null);
            }
        });
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected void clientSyncDataChanged() {
        if (getLevel() != null) {
            setChanged();
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        BoundingBox box = new BoundingBox(getBlockPos());
        devices.forEach(device -> {
            LocalAxonConnection connection = device.upstream();
            if (connection == null) return;
            box.encapsulate(device.upstream().getTargetPos());
        });
        return AABB.of(box).inflate(1);
    }
}
