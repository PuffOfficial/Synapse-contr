package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.block.IFacedAxonBlockEntity;
import com.m_w_k.synapse.api.block.ruleset.ItemTransferRuleset;
import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.AxonType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.UnaryOperator;

public class ItemExposer extends AbstractExposer<IItemHandler, ItemExposer, ItemStack> implements IItemHandler {

    protected ItemTransferRuleset ruleset = new ItemTransferRuleset(Dist.DEDICATED_SERVER);

    public ItemExposer(@NotNull IFacedAxonBlockEntity owner) {
        super(owner);
        ruleset.setChangeListener(this::setDirty);
    }

    protected ItemExposer(@NotNull Direction dir, @NotNull ItemExposer parent) {
        super(dir, parent);
        ruleset.setChangeListener(this::setDirty);
    }

    @Override
    public @Nullable ItemTransferRuleset getRuleset() {
        return ruleset;
    }

    @Override
    protected ItemExposer constructChild(Direction dir) {
        if (dir == null) return this;
        return new ItemExposer(dir, this);
    }

    @Override
    protected @NotNull Tag save() {
        return ItemTransferRuleset.CODEC.encodeStart(NbtOps.INSTANCE, this.ruleset).get()
                .map(UnaryOperator.identity(), e -> ByteTag.valueOf((byte) 0));
    }

    @Override
    protected void load(@NotNull Tag nbt) {
        ItemTransferRuleset.CODEC.parse(NbtOps.INSTANCE, nbt).get()
                .ifLeft(ruleset -> {
                    this.ruleset = ruleset;
                    ruleset.setChangeListener(this::setDirty);
                });
    }

    @Override
    protected AxonType getType() {
        return AxonType.ITEM;
    }

    @Override
    public int getSlots() {
        return getConnections().stream().map(AxonTree.Connection::capability)
                .filter(Objects::nonNull).mapToInt(IItemHandler::getSlots).sum();
    }

    protected <T> T handlerAtSlot(int slot, T fallback, Mapper<T> mapper) {
        for (var connection : getConnections()) {
            if (connection.capability() == null) continue;
            if (slot >= connection.capability().getSlots()) {
                slot -= connection.capability().getSlots();
            } else {
                return mapper.map(slot, connection.capability(), connection);
            }
        }
        return fallback;
    }

    protected long volume(ItemStack stack) {
        return volume(stack, stack.getCount());
    }

    protected long volume(ItemStack stack, int count) {
        return count * 64L / stack.getMaxStackSize();
    }

    protected int count(ItemStack stack, long volume) {
        return (int) (volume * stack.getMaxStackSize() / 64);
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return handlerAtSlot(slot, ItemStack.EMPTY, (s, h, c) -> h.getStackInSlot(s));
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return handlerAtSlot(slot, stack, (s, h, c) -> {
            if (!check(stack, c, true)) return stack;
            ItemStack staack = stack;
            long lim = count(staack, getAllowed(volume(staack), c));
            int decrement = 0;
            if (lim < staack.getCount()) {
                decrement = staack.getCount() - (int) lim;
                staack = staack.copyWithCount((int) lim);
            }
            staack = h.insertItem(s, staack, simulate);
            ItemStack ret = stack.copyWithCount(staack.getCount() + decrement);
            if (!simulate) {
                consumeCapacity(volume(stack, stack.getCount() - ret.getCount()), c);
            }
            return ret;
        });
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return handlerAtSlot(slot, ItemStack.EMPTY, (s, h, c) -> {
            ItemStack stack = h.extractItem(s, amount, true);
            if (stack.isEmpty() || !check(stack, c, false)) return ItemStack.EMPTY;
            int aamount = amount;
            long lim = count(stack, getAllowed(volume(stack), c));
            if (lim < aamount) {
                aamount = (int) lim;
            }
            ItemStack ret = h.extractItem(s, aamount, simulate);
            if (!simulate) consumeCapacity(volume(ret), c);
            return ret;
        });
    }

    @Override
    public int getSlotLimit(int slot) {
        return handlerAtSlot(slot, 0, (s, h, c) -> h.getSlotLimit(s));
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return handlerAtSlot(slot, false, (s, h, c) -> check(stack, c, true) && h.isItemValid(s, stack));
    }

    protected interface Mapper<T> {

        T map(int actualSlot, @NotNull IItemHandler handler, AxonTree.Connection<IItemHandler> connection);
    }
}
