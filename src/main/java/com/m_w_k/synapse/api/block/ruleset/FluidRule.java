package com.m_w_k.synapse.api.block.ruleset;

import com.m_w_k.synapse.api.connect.AxonAddress;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FluidRule implements FluidRuleAccess {

    public static final Codec<FluidRule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AxonAddress.CODEC.fieldOf("address").forGetter(FluidRule::getAddress),
                    Codec.BOOL.fieldOf("matchesIncoming").forGetter(FluidRule::isMatchesIncoming),
                    Codec.BOOL.fieldOf("matchesOutgoing").forGetter(FluidRule::isMatchesOutgoing),
                    Codec.list(FluidStack.CODEC).xmap(l -> l.toArray(new FluidStack[9]), ObjectArrayList::new)
                            .fieldOf("matchStacks").forGetter(r -> r.matchStacks),
                    Codec.BOOL.fieldOf("whitelist").forGetter(FluidRule::isWhitelist),
                    Codec.BOOL.fieldOf("matchNBT").forGetter(FluidRule::isMatchNBT)
            ).apply(instance, FluidRule::new));

    @NotNull
    protected AxonAddress address = new AxonAddress();
    protected boolean matchesIncoming;
    protected boolean matchesOutgoing;

    public final FluidStack[] matchStacks;
    protected boolean whitelist = true;
    protected boolean matchNBT;

    public FluidRule() {
        matchStacks = new FluidStack[9];
        Arrays.fill(matchStacks, FluidStack.EMPTY);
    }

    public FluidRule(@NotNull AxonAddress address, boolean matchesIncoming, boolean matchesOutgoing,
                     FluidStack[] matchStacks, boolean whitelist, boolean matchNBT) {
        this.setAddress(address);
        this.setMatchesIncoming(matchesIncoming);
        this.setMatchesOutgoing(matchesOutgoing);
        this.matchStacks = matchStacks;
        this.setWhitelist(whitelist);
        this.setMatchNBT(matchNBT);
    }

    public boolean matches(FluidStack stack) {
        for (FluidStack match : matchStacks) {
            if (stack.getFluid() != match.getFluid()) continue;
            if (isMatchNBT() && !FluidStack.areFluidStackTagsEqual(stack, match)) continue;
            return isWhitelist();
        }
        return !isWhitelist();
    }

    @Override
    public @NotNull AxonAddress getAddress() {
        return address;
    }

    @Override
    public void setAddress(@NotNull AxonAddress address) {
        this.address = address;
    }

    @Override
    public boolean isMatchesIncoming() {
        return matchesIncoming;
    }

    @Override
    public void setMatchesIncoming(boolean matchesIncoming) {
        this.matchesIncoming = matchesIncoming;
    }

    @Override
    public boolean isMatchesOutgoing() {
        return matchesOutgoing;
    }

    @Override
    public void setMatchesOutgoing(boolean matchesOutgoing) {
        this.matchesOutgoing = matchesOutgoing;
    }

    @Override
    public FluidStack getMatchStack(int index) {
        return matchStacks[index];
    }

    @Override
    public void setMatchStack(int index, FluidStack stack) {
        matchStacks[index] = stack;
    }

    @Override
    public boolean isWhitelist() {
        return whitelist;
    }

    @Override
    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
    }

    @Override
    public boolean isMatchNBT() {
        return matchNBT;
    }

    @Override
    public void setMatchNBT(boolean matchNBT) {
        this.matchNBT = matchNBT;
    }
}
