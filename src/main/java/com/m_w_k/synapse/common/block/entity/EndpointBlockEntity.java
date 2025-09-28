package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.common.block.EndpointBlock;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Endpoints are a network's connection with the rest of the minecraft world, and serve requests
 * to the network from external sources.
 */
public class EndpointBlockEntity extends AxonBlockEntity implements FacedAxonBlockEntity {

    public EndpointBlockEntity(BlockPos pos, BlockState state) {
        super(SynapseBlockEntityRegistry.ENDPOINT_BLOCK.get(), pos, state);
        for (Direction ignored : Direction.values()) {
            devices.add(new LocalConnectorDevice(ConnectorLevel.ENDPOINT));
        }
    }

    @Override
    public int getSlotForFace(Direction face) {
        return face.ordinal();
    }

    @Override
    public @NotNull Vec3 renderOffsetForSlot(int slot) {
        Direction dir = Direction.values()[slot];
        return new Vec3(dir.getStepX() / 2d, dir.getStepY() / 2d, dir.getStepZ() / 2d);
    }

    public boolean activeOnSide(Capability<?> cap, Direction side) {
        if (side == null) return false;
        return getBlockState().getValue(EndpointBlock.PROPERTY_BY_DIRECTION.get(side));
    }

    @Override
    public @Nullable LocalAxonConnection setUpstream(@NotNull AxonType type, @NotNull LocalAxonConnection connection, boolean dropOld) {
        LocalAxonConnection ret = super.setUpstream(type, connection, dropOld);
        updateDevice(type, connection.getAxonType().getCapability(), Direction.values()[connection.getSourceSlot()]);
        return ret;
    }

    public void neighborChanged(Direction side) {
        if (side == null) return;
        getByFace(side).upstream().keySet().forEach(t -> {
            if (activeOnSide(t.getCapability(), side)) updateDevice(t, t.getCapability(), side);
        });
    }

    @Override
    public void onLoad() {
        super.onLoad();
        for (Direction dir : Direction.values()) {
            neighborChanged(dir);
        }
    }

    protected <T> void updateDevice(AxonType type, Capability<T> cap, Direction side) {
        LocalConnectorDevice device = getByFace(side);
        if (getLevel() != null) {
            BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(side));
            device.ensureRegistered(getLevel(), type); // wipe capability data
            if (be != null) {
                // update capability data if present
                be.getCapability(cap, side.getOpposite()).ifPresent(t -> device.ensureRegistered(getLevel(), type, cap, t));
            }
        }
    }
}
