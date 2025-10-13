package com.m_w_k.synapse.api.block.ruleset;

import com.m_w_k.synapse.api.connect.AxonAddress;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ItemRule implements ItemRuleAccess {

    public static final Codec<ItemRule> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    AxonAddress.CODEC.fieldOf("address").forGetter(ItemRule::getAddress),
                    Codec.BOOL.fieldOf("matchesIncoming").forGetter(ItemRule::isMatchesIncoming),
                    Codec.BOOL.fieldOf("matchesOutgoing").forGetter(ItemRule::isMatchesOutgoing),
                    Codec.list(ItemStack.CODEC).xmap(l -> l.toArray(new ItemStack[9]), ObjectArrayList::new)
                            .fieldOf("matchStacks").forGetter(r -> r.matchStacks),
                    Codec.BOOL.fieldOf("whitelist").forGetter(ItemRule::isWhitelist),
                    Codec.BOOL.fieldOf("matchNBT").forGetter(ItemRule::isMatchNBT)
            ).apply(instance, ItemRule::new));

    @NotNull
    protected AxonAddress address = new AxonAddress();
    protected boolean matchesIncoming;
    protected boolean matchesOutgoing;

    public final ItemStack[] matchStacks;
    protected boolean whitelist = true;
    protected boolean matchNBT;

    public ItemRule() {
        matchStacks = new ItemStack[9];
        Arrays.fill(matchStacks, ItemStack.EMPTY);
    }

    public ItemRule(@NotNull AxonAddress address, boolean matchesIncoming, boolean matchesOutgoing,
                    ItemStack[] matchStacks, boolean whitelist, boolean matchNBT) {
        this.setAddress(address);
        this.setMatchesIncoming(matchesIncoming);
        this.setMatchesOutgoing(matchesOutgoing);
        this.matchStacks = matchStacks;
        this.setWhitelist(whitelist);
        this.setMatchNBT(matchNBT);
    }

    public boolean matches(ItemStack stack) {
        for (ItemStack match : matchStacks) {
            if (stack.getItem() != match.getItem()) continue;
            if (isMatchNBT() && !ItemStack.isSameItemSameTags(match, stack)) continue;
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
    public ItemStack getMatchStack(int index) {
        return matchStacks[index];
    }

    @Override
    public void setMatchStack(int index, ItemStack stack) {
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
