package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.block.entity.AxonBlockEntity;
import com.m_w_k.synapse.common.block.entity.FacedAxonBlockEntity;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FluidExposer extends AbstractExposer<IFluidHandler, FluidExposer> implements IFluidHandler {

    public FluidExposer(@NotNull FacedAxonBlockEntity owner) {
        super(owner);
    }

    protected FluidExposer(@NotNull Direction dir, @NotNull FluidExposer parent) {
        super(dir, parent);
    }

    @Override
    protected FluidExposer constructChild(Direction dir) {
        return new FluidExposer(dir, this);
    }

    @Override
    protected AxonType getType() {
        return AxonType.FLUID;
    }

    @Override
    public int getTanks() {
        return getConnections().stream().map(AxonTree.Connection::capability).filter(Objects::nonNull)
                .mapToInt(IFluidHandler::getTanks).sum();
    }

    protected <T> T handlerAtTank(int tank, T fallback, Mapper<T> mapper) {
        for (var connection : getConnections()) {
            if (connection.capability() == null) continue;
            if (tank > connection.capability().getTanks()) {
                tank -= connection.capability().getTanks();
            } else {
                return mapper.map(tank, connection.capability(), connection);
            }
        }
        return fallback;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return handlerAtTank(tank, FluidStack.EMPTY, (s, h, c) -> h.getFluidInTank(s));
    }

    @Override
    public int getTankCapacity(int tank) {
        return handlerAtTank(tank, 0, (s, h, c) -> h.getTankCapacity(s));
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return handlerAtTank(tank, false, (s, h, c) -> h.isFluidValid(s, stack));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int fill = 0;
        FluidStack copy = resource.copy();
        for (var connection : getConnections()) {
            if (connection.capability() == null) continue;
            copy.setAmount(resource.getAmount() - fill);
            fill += connection.capability().fill(copy, action);
            if (fill >= resource.getAmount()) break;
        }
        return fill;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        int drain = 0;
        FluidStack copy = resource.copy();
        for (var connection : getConnections()) {
            if (connection.capability() == null) continue;
            copy.setAmount(resource.getAmount() - drain);
            drain += connection.capability().drain(copy, action).getAmount();
            if (drain >= resource.getAmount()) break;
        }
        copy.setAmount(drain);
        return copy;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        int drain = 0;
        FluidStack pick = FluidStack.EMPTY;
        for (var connection : getConnections()) {
            if (connection.capability() == null) continue;
            if (!pick.isEmpty()) {
                pick.setAmount(maxDrain - drain);
                drain += connection.capability().drain(pick, action).getAmount();
            } else {
                pick = connection.capability().drain(maxDrain, action);
                drain += pick.getAmount();
            }
            if (drain >= maxDrain) break;
        }
        pick.setAmount(drain);
        return pick;
    }

    protected interface Mapper<T> {

        T map(int actualTank, @NotNull IFluidHandler handler, AxonTree.Connection<IFluidHandler> connection);
    }
}
