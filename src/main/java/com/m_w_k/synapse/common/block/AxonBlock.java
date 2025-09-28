package com.m_w_k.synapse.common.block;

import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectionType;
import com.m_w_k.synapse.common.block.entity.AxonBlockEntity;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.m_w_k.synapse.common.item.AxonItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public abstract class AxonBlock extends BaseEntityBlock {
    public AxonBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level,
                                          @NotNull BlockPos pos, @NotNull Player player,
                                          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        if (!(stack.getItem() instanceof AxonItem iAxon)) return InteractionResult.PASS;
        BlockEntity b = level.getBlockEntity(pos);
        if (!(b instanceof AxonBlockEntity usAxon)) return InteractionResult.PASS;

        BlockPos connect = iAxon.getConnectPos(stack);
        if (pos.equals(connect)) return InteractionResult.FAIL;
        if (connect == null) {
            iAxon.setConnectPos(stack, pos);
            iAxon.setConnectSlot(stack, determineHitSlot(state, level, pos, player, hand, hit));
            return InteractionResult.SUCCESS;
        }
        BlockEntity a = level.getBlockEntity(connect);
        if (!(a instanceof AxonBlockEntity themAxon)) return InteractionResult.PASS;

        AxonType type = iAxon.getType();
        int themSlot = iAxon.getConnectSlot(stack);
        int usSlot = determineHitSlot(state, level, pos, player, hand, hit);
        LocalConnectorDevice us = usAxon.getBySlot(usSlot);
        LocalConnectorDevice them = themAxon.getBySlot(themSlot);
        ConnectionType direction = us.level().typeOf(them.level());
        us.ensureRegistered(level, type);
        them.ensureRegistered(level, type);
        if (direction.upstream()) {
            if (us.hasUpstream(type)) return InteractionResult.FAIL;
            LocalAxonConnection connection = new LocalAxonConnection(iAxon, usSlot, usAxon.renderOffsetForSlot(usSlot),
                    connect, themSlot, themAxon.renderOffsetForSlot(themSlot), type, direction);
            if (iAxon.consumeToPlace(connection, stack, player, false)) {
                var tree = AxonTree.load(level, type, type.getCapability());
                if (tree.isEmpty() || tree.get().connect(us.treeID(), null, them.treeID(), null) == null) {
                    return InteractionResult.FAIL;
                }
                iAxon.consumeToPlace(connection, stack, player, true);
                usAxon.setUpstream(type, connection, !player.isCreative());
                iAxon.clearConnectData(stack);
                return InteractionResult.SUCCESS;
            }
        } else if (direction.downstream()) {
            if (them.hasUpstream(type)) return InteractionResult.FAIL;
            LocalAxonConnection connection = new LocalAxonConnection(iAxon, themSlot, themAxon.renderOffsetForSlot(themSlot),
                    pos, usSlot, usAxon.renderOffsetForSlot(usSlot), type, direction.flip());
            if (iAxon.consumeToPlace(connection, stack, player, false)) {
                var tree = AxonTree.load(level, type, type.getCapability());
                if (tree.isEmpty() || tree.get().connect(us.treeID(), null, them.treeID(), null) == null) {
                    return InteractionResult.FAIL;
                }
                iAxon.consumeToPlace(connection, stack, player, true);
                themAxon.setUpstream(type, connection, !player.isCreative());
                iAxon.clearConnectData(stack);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    protected int determineHitSlot(@NotNull BlockState state, @NotNull Level level,
                                   @NotNull BlockPos pos, @NotNull Player player,
                                   @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        return 0;
    }
}
