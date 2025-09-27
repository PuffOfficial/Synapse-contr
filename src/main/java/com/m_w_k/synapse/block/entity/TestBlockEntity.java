package com.m_w_k.synapse.block.entity;

import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class TestBlockEntity extends BlockEntity {
    public TestBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(SynapseBlockEntityRegistry.TEST_BLOCK.get(), p_155229_, p_155230_);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return AABB.ofSize(getBlockPos().getCenter(), 10, 10, 10);
    }
}
