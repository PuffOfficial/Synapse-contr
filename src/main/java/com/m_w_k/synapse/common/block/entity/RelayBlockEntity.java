package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.SynapseUtil;
import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.api.connect.DeviceDataKey;
import com.m_w_k.synapse.common.block.RelayBlock;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Relays are a critical component of any large network that allows extending the effective
 * connection length of a distributor.
 */
public class RelayBlockEntity extends AxonBlockEntity {

    public RelayBlockEntity(BlockPos pos, BlockState state) {
        super(SynapseBlockEntityRegistry.RELAY_BLOCK.get(), pos, state);
        for (var pair : AxonDeviceDefinitions.RELAYS_INV) {
            devices.add(new LocalConnectorDevice(pair.value(), ConnectorLevel.RELAY));
        }
    }

    @Override
    public @Nullable LocalAxonConnection setUpstream(@NotNull LocalAxonConnection connection, boolean dropOld) {
        if (getLevel() != null) {
            BlockEntity be = getLevel().getBlockEntity(connection.getTargetPos());
            if (be instanceof IAxonBlockEntity a) {
                LocalConnectorDevice device = getBySlot(connection.getSourceSlot());
                device.getData().put(DeviceDataKey.RELAYING, SynapseUtil.actualLevel(a.getBySlot(connection.getTargetSlot())));
                var treeDevice = device.cache();
                if (treeDevice != null && treeDevice.hasUpstream()) {
                    treeDevice.getData().put(DeviceDataKey.RELAYING, SynapseUtil.actualLevel(treeDevice.getUpstream()));
                }
            }
        }
        return super.setUpstream(connection, dropOld);
    }

    @Override
    public boolean allowsUpstream(int slot, LocalConnectorDevice upstream) {
        if (!super.allowsUpstream(slot, upstream)) return false;
        LocalConnectorDevice us = getBySlot(slot);
        if (SynapseUtil.actualLevel(us) != ConnectorLevel.RELAY) {
            // only allow promoting our actual level to prevent broken behavior
            if (SynapseUtil.actualLevel(us).getPrio() > SynapseUtil.actualLevel(upstream).getPrio()) return false;
        }
        return ConnectorLevel.ADDRESS_SPACE.contains(SynapseUtil.actualLevel(upstream));
    }

    @Override
    public boolean allowsDownstream(int slot, LocalConnectorDevice downstream) {
        return super.allowsDownstream(slot, downstream) && getBySlot(slot).upstream() != null;
    }

    @Override
    public @NotNull String getNameBySlot(int slot) {
        return Integer.toString(slot);
    }

    @Override
    public boolean slotIsActive(int slot) {
        return getBlockState().getValue(RelayBlock.RELAYS) > AxonDeviceDefinitions.RELAYS_INV.get(slot).firstInt();
    }
}
