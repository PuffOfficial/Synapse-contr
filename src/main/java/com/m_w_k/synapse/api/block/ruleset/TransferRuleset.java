package com.m_w_k.synapse.api.block.ruleset;

import com.m_w_k.synapse.api.connect.AxonAddress;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.client.gui.AbstractConnectorScreen;
import com.m_w_k.synapse.client.gui.RulesetWidget;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.function.Consumer;

public interface TransferRuleset {

    @NotNull TransferRuleset createNew(Dist dist);

    @NotNull Consumer<TransferRuleset> syncAction(FriendlyByteBuf buf, Dist destination);

    @NotNull Consumer<FriendlyByteBuf> clientSyncData();

    @NotNull AxonType getType();

    @OnlyIn(Dist.CLIENT)
    @NotNull Consumer<FriendlyByteBuf> serverSyncData(@NotNull RulesetWidget widget);

    @OnlyIn(Dist.CLIENT)
    boolean hasPendingSync(@NotNull RulesetWidget widget);

    @OnlyIn(Dist.CLIENT)
    @NotNull RulesetWidget createWidget(AbstractConnectorScreen<?> parent, int x, int y);

    interface QueryableRuleset<T> extends TransferRuleset {

        @UnmodifiableView
        @Nullable AxonAddress getMatchingAddress(@NotNull T query, boolean incoming);

        @NotNull
        Collection<AxonAddress> getAllPossibleAddresses();
    }
}
