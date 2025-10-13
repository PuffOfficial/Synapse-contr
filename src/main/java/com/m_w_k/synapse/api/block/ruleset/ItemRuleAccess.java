package com.m_w_k.synapse.api.block.ruleset;

import com.m_w_k.synapse.api.connect.AxonAddress;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemRuleAccess {

    @NotNull AxonAddress getAddress();

    void setAddress(@NotNull AxonAddress address);

    boolean isMatchesIncoming();

    void setMatchesIncoming(boolean matchesIncoming);

    boolean isMatchesOutgoing();

    void setMatchesOutgoing(boolean matchesOutgoing);

    ItemStack getMatchStack(int index);

    void setMatchStack(int index, ItemStack stack);

    boolean isWhitelist();

    void setWhitelist(boolean whitelist);

    boolean isMatchNBT();

    void setMatchNBT(boolean matchNBT);
}
