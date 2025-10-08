package com.m_w_k.synapse.common.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.m_w_k.synapse.SynapseUtil;
import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.common.block.entity.EndpointBlockEntity;
import com.m_w_k.synapse.common.item.AxonItem;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
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
import java.util.EnumMap;
import java.util.Map;

public class EndpointBlock extends AxonBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION =
            new EnumMap<>(PipeBlock.PROPERTY_BY_DIRECTION);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty ENDPOINTS = IntegerProperty.create("endpoints", 1, 6);

    public EndpointBlock(Properties p_49795_) {
        super(p_49795_);
        BlockState def = this.stateDefinition.any().setValue(ENDPOINTS, 1)
                .setValue(WATERLOGGED, Boolean.FALSE);
        for (BooleanProperty prop : PROPERTY_BY_DIRECTION.values()) {
            def = def.setValue(prop, Boolean.FALSE);
        }
        this.registerDefaultState(def);
    }

    @Override
    protected int determineHitSlot(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof AxonItem iAxon) {
            return AxonDeviceDefinitions.endpoint(iAxon.getType(), hit.getDirection());
        }
        return hit.getDirection().ordinal();
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        level.getBlockEntity(pos, SynapseBlockEntityRegistry.ENDPOINT_BLOCK.get()).ifPresent(be -> be.neighborChanged(SynapseUtil.facingTo(pos, neighbor)));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockstate = ctx.getLevel().getBlockState(ctx.getClickedPos());
        Direction face = ctx.getClickedFace();
        // TODO test this behavior when collision boxes are finalized
        if (ctx.isInside()) face = face.getOpposite();
        if (!blockstate.is(this)) {
            face = face.getOpposite();
            FluidState fluidstate = ctx.getLevel().getFluidState(ctx.getClickedPos());
            boolean flag = fluidstate.getType() == Fluids.WATER;
            blockstate = defaultBlockState().setValue(WATERLOGGED, flag);
        } else {
            blockstate = blockstate.setValue(ENDPOINTS, Math.min(6, blockstate.getValue(ENDPOINTS) + 1));
        }
        return blockstate.setValue(PROPERTY_BY_DIRECTION.get(face), true);
    }

    @Override
    public boolean canBeReplaced(@NotNull BlockState state, BlockPlaceContext ctx) {
        return !ctx.isSecondaryUseActive() && ctx.getItemInHand().is(this.asItem()) &&
                !state.getValue(PROPERTY_BY_DIRECTION.get(ctx.getClickedFace())) || super.canBeReplaced(state, ctx);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> def) {
        def.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, BlockStateProperties.WATERLOGGED, ENDPOINTS);
    }

    @Override
    public @NotNull EndpointBlockEntity newBlockEntity(@NotNull BlockPos p_153215_, @NotNull BlockState p_153216_) {
        return new EndpointBlockEntity(p_153215_, p_153216_);
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
