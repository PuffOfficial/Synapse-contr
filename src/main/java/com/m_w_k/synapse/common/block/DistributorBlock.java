package com.m_w_k.synapse.common.block;

import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.common.block.entity.DistributorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class DistributorBlock extends AxonBlock {
    protected static final VoxelShape SHAPE = Block.box(1.0D, 1.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    private final @NotNull ConnectorLevel tier;

    public DistributorBlock(Properties p_49795_, @NotNull ConnectorLevel tier) {
        super(p_49795_);
        this.tier = tier;
    }

    @Override
    public @NotNull DistributorBlockEntity newBlockEntity(@NotNull BlockPos p_153215_, @NotNull BlockState p_153216_) {
        return new DistributorBlockEntity(p_153215_, p_153216_, tier);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return SHAPE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
        return super.propagatesSkylightDown(p_49928_, p_49929_, p_49930_);
    }
}
