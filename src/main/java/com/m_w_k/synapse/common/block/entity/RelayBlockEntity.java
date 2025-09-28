package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Relays are a critical component of any large network that allows extending the effective
 * connection length of a distributor.
 */
// TODO NYI
public class RelayBlockEntity extends AxonBlockEntity {

    public RelayBlockEntity(BlockPos pos, BlockState state) {
        super(SynapseBlockEntityRegistry.RELAY_BLOCK.get(), pos, state);
    }
}
