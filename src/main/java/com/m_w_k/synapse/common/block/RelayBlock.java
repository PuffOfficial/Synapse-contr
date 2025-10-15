package com.m_w_k.synapse.common.block;

import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.common.block.entity.AxonBlockEntity;
import com.m_w_k.synapse.common.block.entity.RelayBlockEntity;
import com.m_w_k.synapse.common.item.AxonItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class RelayBlock extends AxonBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty RELAYS = IntegerProperty.create("relays", 1, 4);

    public RelayBlock(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any().setValue(RELAYS, 1)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    protected boolean hasInteractMenu() {
        return false;
    }

    @Override
    protected int determineHitSlot(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        // TODO
        if (player.getItemInHand(hand).getItem() instanceof AxonItem iAxon) {
            return AxonDeviceDefinitions.relay(iAxon.getType(), 0);
        }
        return 0;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockstate = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (!blockstate.is(this)) {
            FluidState fluidstate = ctx.getLevel().getFluidState(ctx.getClickedPos());
            boolean flag = fluidstate.getType() == Fluids.WATER;
            return defaultBlockState().setValue(WATERLOGGED, flag);
        } else {
            return blockstate.setValue(RELAYS, Math.min(4, blockstate.getValue(RELAYS) + 1));
        }
    }

    @Override
    public boolean canBeReplaced(@NotNull BlockState state, BlockPlaceContext ctx) {
        return !ctx.isSecondaryUseActive() && ctx.getItemInHand().is(this.asItem()) &&
                state.getValue(RELAYS) < 4 || super.canBeReplaced(state, ctx);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> def) {
        def.add(BlockStateProperties.WATERLOGGED, RELAYS);
    }

    @Override
    public @NotNull RelayBlockEntity newBlockEntity(@NotNull BlockPos p_153215_, @NotNull BlockState p_153216_) {
        return new RelayBlockEntity(p_153215_, p_153216_);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_51039_, @NotNull BlockGetter p_51040_, @NotNull BlockPos p_51041_) {
        return p_51039_.getFluidState().isEmpty();
    }

    @Override
    public boolean isPathfindable(@NotNull BlockState p_60475_, @NotNull BlockGetter p_60476_, @NotNull BlockPos p_60477_, @NotNull PathComputationType p_60478_) {
        return false;
    }
}
