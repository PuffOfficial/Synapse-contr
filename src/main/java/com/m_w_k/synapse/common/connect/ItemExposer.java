package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.block.entity.AxonBlockEntity;
import com.m_w_k.synapse.common.block.entity.FacedAxonBlockEntity;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class ItemExposer extends AbstractExposer<IItemHandler, ItemExposer> implements IItemHandler {

    public ItemExposer(@NotNull FacedAxonBlockEntity owner) {
        super(owner);
    }

    protected ItemExposer(@NotNull Direction dir, @NotNull ItemExposer parent) {
        super(dir, parent);
    }

    @Override
    protected ItemExposer constructChild(Direction dir) {
        return new ItemExposer(dir, this);
    }

    @Override
    protected AxonType getType() {
        return AxonType.ITEM;
    }

    @Override
    public int getSlots() {
        return getConnections().stream().map(AxonTree.Connection::capability).filter(Objects::nonNull)
                .mapToInt(IItemHandler::getSlots).sum();
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

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return handlerAtSlot(slot, ItemStack.EMPTY, (s, h, c) -> h.getStackInSlot(s));
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return handlerAtSlot(slot, stack, (s, h, c) -> {
            ItemStack staack = stack;
            long lim = getAllowed(staack.getCount(), c);
            if (lim < staack.getCount()) {
                staack = staack.copyWithCount((int) lim);
            }
            ItemStack ret = h.insertItem(s, staack, simulate);
            if (!simulate) consumeCapacity(staack.getCount(), c);
            return ret;
        });
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return handlerAtSlot(slot, ItemStack.EMPTY, (s, h, c) -> {
            int aamount = amount;
            long lim = getAllowed(aamount, c);
            if (lim < aamount) {
                aamount = (int) lim;
            }
            ItemStack ret = h.extractItem(s, aamount, simulate);
            if (!simulate) consumeCapacity(aamount, c);
            return ret;
        });
    }

    @Override
    public int getSlotLimit(int slot) {
        return handlerAtSlot(slot, 0, (s, h, c) -> h.getSlotLimit(s));
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return handlerAtSlot(slot, false, (s, h, c) -> h.isItemValid(s, stack));
    }

    protected interface Mapper<T> {

        T map(int actualSlot, @NotNull IItemHandler handler, AxonTree.Connection<IItemHandler> connection);
    }
}
