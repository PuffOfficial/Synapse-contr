package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.block.ruleset.FluidTransferRuleset;
import com.m_w_k.synapse.api.block.ruleset.ItemTransferRuleset;
import com.m_w_k.synapse.api.block.ruleset.TransferRuleset;
import com.m_w_k.synapse.api.connect.AxonTree;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.block.IFacedAxonBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.UnaryOperator;

public class FluidExposer extends AbstractExposer<IFluidHandler, FluidExposer, FluidStack> implements IFluidHandler {

    protected FluidTransferRuleset ruleset = new FluidTransferRuleset(Dist.DEDICATED_SERVER);

    public FluidExposer(@NotNull IFacedAxonBlockEntity owner) {
        super(owner);
    }

    protected FluidExposer(@NotNull Direction dir, @NotNull FluidExposer parent) {
        super(dir, parent);
    }

    @Override
    protected FluidExposer constructChild(Direction dir) {
        if (dir == null) return this;
        return new FluidExposer(dir, this);
    }

    @Override
    protected @NotNull Tag save() {
        return FluidTransferRuleset.CODEC.encodeStart(NbtOps.INSTANCE, this.ruleset).get()
                .map(UnaryOperator.identity(), e -> ByteTag.valueOf((byte) 0));
    }

    @Override
    protected void load(@NotNull Tag nbt) {
        FluidTransferRuleset.CODEC.parse(NbtOps.INSTANCE, nbt).get()
                .ifLeft(ruleset -> {
                    this.ruleset = ruleset;
                    ruleset.setChangeListener(this::setDirty);
                });
    }

    @Override
    public TransferRuleset.@Nullable QueryableRuleset<FluidStack> getRuleset() {
        return ruleset;
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
        return handlerAtTank(tank, false, (s, h, c) -> h.isFluidValid(s, stack) && check(stack, c, true));
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int fill = 0;
        FluidStack copy = resource.copy();
        for (var connection : getConnections()) {
            if (connection.capability() == null) continue;
            if (!check(copy, connection, true)) continue;
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
            if (!check(copy, connection, false)) continue;
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
                if (!check(pick, connection, false)) continue;
                pick.setAmount(maxDrain - drain);
                drain += connection.capability().drain(pick, action).getAmount();
            } else {
                pick = connection.capability().drain(maxDrain, action);
                if (!check(pick, connection, false)) {
                    pick = FluidStack.EMPTY;
                    continue;
                }
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
