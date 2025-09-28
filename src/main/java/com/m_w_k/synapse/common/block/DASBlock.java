package com.m_w_k.synapse.common.block;

import com.m_w_k.synapse.common.block.entity.DASBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class DASBlock extends AxonBlock {

    public DASBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public @NotNull DASBlockEntity newBlockEntity(@NotNull BlockPos p_153215_, @NotNull BlockState p_153216_) {
        return new DASBlockEntity(p_153215_, p_153216_);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState p_49232_) {
        return RenderShape.MODEL;
    }
}
