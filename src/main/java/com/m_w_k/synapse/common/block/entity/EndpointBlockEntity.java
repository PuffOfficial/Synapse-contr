package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.IFacedAxonBlockEntity;
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
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Endpoints are a network's connection with the rest of the minecraft world, and serve requests
 * to the network from external sources.
 */
public class EndpointBlockEntity extends AxonBlockEntity implements IFacedAxonBlockEntity {

    public EndpointBlockEntity(BlockPos pos, BlockState state) {
        super(SynapseBlockEntityRegistry.ENDPOINT_BLOCK.get(), pos, state);
        for (var pair : AxonDeviceDefinitions.ENDPOINTS_INV) {
            devices.add(new LocalConnectorDevice(pair.key(), ConnectorLevel.ENDPOINT));
        }
    }

    @Override
    public int getSlotForFace(@NotNull Direction face, @NotNull AxonType type) {
        return AxonDeviceDefinitions.endpoint(type, face);
    }

    @Override
    public @NotNull String getNameBySlot(int slot) {
        var pair = AxonDeviceDefinitions.ENDPOINTS_INV.get(slot);
        return pair.value().name() + "_" + pair.key().name();
    }

    @Override
    public boolean slotIsActive(int slot) {
        return activeOnSide(null, AxonDeviceDefinitions.ENDPOINTS_INV.get(slot).right());
    }

    @Override
    public @NotNull Vec3 renderOffsetForSlot(int slot) {
        Direction dir = AxonDeviceDefinitions.ENDPOINTS_INV.get(slot).right();
        return new Vec3(dir.getStepX() / 2d, dir.getStepY() / 2d, dir.getStepZ() / 2d);
    }

    public boolean activeOnSide(@Nullable Capability<?> cap, Direction side) {
        if (side == null) return false;
        return getBlockState().getValue(EndpointBlock.PROPERTY_BY_DIRECTION.get(side));
    }

    @Override
    public @Nullable LocalAxonConnection setUpstream(@NotNull LocalAxonConnection connection, boolean dropOld) {
        LocalAxonConnection ret = super.setUpstream(connection, dropOld);
        updateDevice(connection.getAxonType(), connection.getAxonType().getCapability(), AxonDeviceDefinitions.ENDPOINTS_INV.get(connection.getSourceSlot()).right());
        return ret;
    }

    public void neighborChanged(Direction side) {
        if (side == null) return;
        for (AxonType type : AxonType.values()) {
            if (activeOnSide(type.getCapability(), side)) updateDevice(type, type.getCapability(), side);
        }

    }

    @Override
    public void onLoad() {
        super.onLoad();
        for (Direction dir : Direction.values()) {
            neighborChanged(dir);
        }
    }

    protected <T> void updateDevice(AxonType type, Capability<T> cap, Direction side) {
        LocalConnectorDevice device = getByFace(side, type);
        if (getLevel() != null) {
            BlockEntity be = getLevel().getBlockEntity(getBlockPos().relative(side));
            device.ensureRegistered(getLevel()); // wipe capability data
            if (be != null) {
                // update capability data if present
                be.getCapability(cap, side.getOpposite()).ifPresent(t -> device.ensureRegistered(getLevel(), cap, t));
            }
        }
    }
}
