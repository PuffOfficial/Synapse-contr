package com.m_w_k.synapse.common.block;

import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectionType;
import com.m_w_k.synapse.common.block.entity.AxonBlockEntity;
import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.m_w_k.synapse.common.item.AxonItem;
import com.m_w_k.synapse.common.menu.BasicConnectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AxonBlock extends BaseEntityBlock {
    public AxonBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level,
                                          @NotNull BlockPos pos, @NotNull Player player,
                                          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        BlockEntity b = level.getBlockEntity(pos);
        if (!(b instanceof AxonBlockEntity usAxon)) return InteractionResult.PASS;

        if (!(stack.getItem() instanceof AxonItem iAxon)) {
            if (hand == InteractionHand.OFF_HAND || !hasInteractMenu()) return InteractionResult.PASS;
            if (player instanceof ServerPlayer s) {
                openInteractMenu(s, level, state, pos, usAxon);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        BlockPos connect = iAxon.getConnectPos(stack);
        if (pos.equals(connect)) return InteractionResult.FAIL;
        int usSlot = determineHitSlot(state, level, pos, player, hand, hit);
        if (!usAxon.slotIsActive(usSlot)) return InteractionResult.FAIL;
        if (connect == null) {
            iAxon.setConnectPos(stack, pos);
            iAxon.setConnectSlot(stack, usSlot);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        BlockEntity a = level.getBlockEntity(connect);
        if (!(a instanceof AxonBlockEntity themAxon)) return InteractionResult.PASS;

        AxonType type = iAxon.getType();
        int themSlot = iAxon.getConnectSlot(stack);
        LocalConnectorDevice us = usAxon.getBySlot(usSlot);
        if (us.type() != type) {
            iAxon.clearConnectData(stack);
            return InteractionResult.FAIL;
        }
        LocalConnectorDevice them = themAxon.getBySlot(themSlot);
        if (them.type() != type) {
            iAxon.clearConnectData(stack);
            return InteractionResult.FAIL;
        }
        ConnectionType direction = us.level().typeOf(them.level());
        us.ensureRegistered(level);
        them.ensureRegistered(level);
        if (direction.upstream()) {
            if (us.upstream() != null) return InteractionResult.FAIL;
            LocalAxonConnection connection = new LocalAxonConnection(iAxon, usSlot, usAxon.renderOffsetForSlot(usSlot),
                    connect, themSlot, themAxon.renderOffsetForSlot(themSlot), type, direction);
            if (iAxon.consumeToPlace(connection, stack, player, false)) {
                var tree = AxonTree.load(level, type, type.getCapability());
                if (tree.isEmpty() || tree.get().connect(us.treeID(), null, them.treeID(), null) == null) {
                    return InteractionResult.FAIL;
                }
                iAxon.consumeToPlace(connection, stack, player, true);
                usAxon.setUpstream(connection, !player.isCreative());
                iAxon.clearConnectData(stack);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        } else if (direction.downstream()) {
            if (them.upstream() != null) return InteractionResult.FAIL;
            LocalAxonConnection connection = new LocalAxonConnection(iAxon, themSlot, themAxon.renderOffsetForSlot(themSlot),
                    pos, usSlot, usAxon.renderOffsetForSlot(usSlot), type, direction.flip());
            if (iAxon.consumeToPlace(connection, stack, player, false)) {
                var tree = AxonTree.load(level, type, type.getCapability());
                if (tree.isEmpty() || tree.get().connect(us.treeID(), null, them.treeID(), null) == null) {
                    return InteractionResult.FAIL;
                }
                iAxon.consumeToPlace(connection, stack, player, true);
                themAxon.setUpstream(connection, !player.isCreative());
                iAxon.clearConnectData(stack);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    protected boolean hasInteractMenu() {
        return true;
    }

    protected void openInteractMenu(@NotNull ServerPlayer player, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull IAxonBlockEntity be) {
        MenuProvider prov = getMenuProvider(state, level, pos);
        if (prov != null) NetworkHooks.openScreen(player, prov, BasicConnectorMenu.writer(be));
    }

    @Override
    public @Nullable MenuProvider getMenuProvider(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IAxonBlockEntity a) {
            return new SimpleMenuProvider(
                    (containerId, playerInventory, p) -> BasicConnectorMenu.of(containerId, playerInventory, a),
                    Component.translatable("synapse.menu.title.basic_connector"));
        }
        return null;
    }

    protected int determineHitSlot(@NotNull BlockState state, @NotNull Level level,
                                   @NotNull BlockPos pos, @NotNull Player player,
                                   @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof AxonItem iAxon) {
            return AxonDeviceDefinitions.standard(iAxon.getType());
        }
        return 0;
    }
}
