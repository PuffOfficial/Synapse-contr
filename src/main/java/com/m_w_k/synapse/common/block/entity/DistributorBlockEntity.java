package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Distributors are the backbone of any network, connecting components together with many to many functionality.
 */
public class DistributorBlockEntity extends AxonBlockEntity {

    public DistributorBlockEntity(BlockPos pos, BlockState state, @NotNull ConnectorLevel tier) {
        super(SynapseBlockEntityRegistry.DISTRIBUTOR_BLOCK.get(), pos, state, tier);
    }
}
