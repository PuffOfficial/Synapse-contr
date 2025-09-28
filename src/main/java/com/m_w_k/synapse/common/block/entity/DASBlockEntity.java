package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Distributor Alias System (DAS for short) allows for associating "aliases" with addresses.
 * For example, the "cobblestone" alias could be associated with an address, and then
 * configuration allows for sending any request involving cobblestone to the cobblestone alias.
 */
public class DASBlockEntity extends AxonBlockEntity {
    public DASBlockEntity(BlockPos pos, BlockState state) {
        super(SynapseBlockEntityRegistry.DAS_BLOCK.get(), pos, state, ConnectorLevel.ENDPOINT);
    }
}
