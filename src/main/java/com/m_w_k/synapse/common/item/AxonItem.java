package com.m_w_k.synapse.common.item;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.connect.LocalAxonConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AxonItem extends Item {
    protected final @NotNull AxonType type;

    public AxonItem(Properties p_41383_, @NotNull AxonType type) {
        super(p_41383_);
        this.type = type;
    }

    protected @NotNull CompoundTag connectTag(@NotNull ItemStack stack) {
        return stack.getOrCreateTagElement("Connect");
    }

    /**
     * Returns whether the stack has connect data.
     * @param stack the stack in question
     * @return whether connect data is present.
     */
    public boolean hasConnectData(@NotNull ItemStack stack) {
        return stack.getTagElement("Connect") != null;
    }

    /**
     * Used to clear connection data. Removes the associated NBT tag
     * @param stack the stack in question
     */
    public void clearConnectData(@NotNull ItemStack stack) {
        stack.removeTagKey("Connect");
    }

    /**
     * Used to set connection data to form a connection.
     * @param stack the stack in question
     * @param pos the block pos to connect to
     */
    public void setConnectPos(@NotNull ItemStack stack, @Nullable BlockPos pos) {
        CompoundTag tag = connectTag(stack);
        if (pos == null) {
            tag.remove("Pos");
            return;
        }
        tag.put("Pos", NbtUtils.writeBlockPos(pos));
    }

    /**
     * Used to retrieve connection data to form a connection.
     * @param stack the stack in question
     * @return the block pos to connect to
     */
    public @Nullable BlockPos getConnectPos(@NotNull ItemStack stack) {
        CompoundTag tag = connectTag(stack);
        if (!tag.contains("Pos")) return null;
        return NbtUtils.readBlockPos(tag.getCompound("Pos"));
    }

    /**
     * Used to set connection data to form a connection.
     * @param stack the stack in question
     * @param slot the slot to connect to
     */
    public void setConnectSlot(@NotNull ItemStack stack, int slot) {
        CompoundTag tag = connectTag(stack);
        if (slot == Integer.MIN_VALUE) {
            tag.remove("Slot");
            return;
        }
        tag.putInt("Slot", slot);
    }

    /**
     * Used to retrieve connection data to form a connection.
     * @param stack the stack in question
     * @return the slot to connect to
     */
    public int getConnectSlot(@NotNull ItemStack stack) {
        CompoundTag tag = connectTag(stack);
        if (!tag.contains("Slot")) return Integer.MIN_VALUE;
        return tag.getInt("Slot");
    }

    public @NotNull AxonType getType() {
        return type;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (player.isCrouching() || player.pick(player.getBlockReach(), 1, false).getType() == HitResult.Type.MISS) {
            ItemStack stack = player.getItemInHand(hand);
            if (hasConnectData(stack)) {
                clearConnectData(stack);
                return InteractionResultHolder.success(stack);
            }
        }
        return super.use(level, player, hand);
    }

    /**
     * Called when a connection is removed to determine what should drop. Can be {@link ItemStack#EMPTY}.
     * @return itemstack that should be dropped when a cable created with this item is removed
     */
    public @NotNull ItemStack getItemWhenRemoved(@NotNull LocalAxonConnection removed) {
        return new ItemStack(this);
    }

    /**
     * Called to determine if a connection should be allowed, and to consume items if allowed.
     * @param placing the connection currently being placed
     * @param stack the itemstack doing the placing
     * @param player the player doing the placing
     * @param consume whether consumptions should occur.
     * @return whether the connection should be allowed
     */
    public boolean consumeToPlace(@NotNull LocalAxonConnection placing, @NotNull ItemStack stack, @NotNull Player player, boolean consume) {
        if (stack.isEmpty()) return false;
        if (consume && !player.isCreative()) stack.shrink(1);
        return true;
    }
}
