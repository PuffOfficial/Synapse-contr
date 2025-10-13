package com.m_w_k.synapse.api.block.ruleset;

import com.m_w_k.synapse.api.connect.AxonAddress;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public interface FluidRuleAccess {

    @NotNull AxonAddress getAddress();

    void setAddress(@NotNull AxonAddress address);

    boolean isMatchesIncoming();

    void setMatchesIncoming(boolean matchesIncoming);

    boolean isMatchesOutgoing();

    void setMatchesOutgoing(boolean matchesOutgoing);

    FluidStack getMatchStack(int index);

    void setMatchStack(int index, FluidStack stack);

    boolean isWhitelist();

    void setWhitelist(boolean whitelist);

    boolean isMatchNBT();

    void setMatchNBT(boolean matchNBT);
}
