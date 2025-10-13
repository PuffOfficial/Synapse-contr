package com.m_w_k.synapse.api.block.ruleset;

import com.m_w_k.synapse.api.connect.AxonAddress;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.client.gui.AbstractConnectorScreen;
import com.m_w_k.synapse.client.gui.FluidRulesetWidget;
import com.m_w_k.synapse.client.gui.RulesetWidget;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class FluidTransferRuleset implements TransferRuleset.QueryableRuleset<FluidStack> {

    public static final Codec<FluidTransferRuleset> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(FluidRule.CODEC).fieldOf("rules").forGetter(r -> r.rules)
                    ).apply(instance, FluidTransferRuleset::new));

    protected final @NotNull List<FluidRule> rules;
    private final Dist dist;

    protected List<Consumer<FriendlyByteBuf>> toSync = new ObjectArrayList<>();

    protected Runnable changeListener = () -> {};

    public FluidTransferRuleset(Dist dist) {
        this.dist = dist;
        this.rules = new ObjectArrayList<>();
        this.rules.add(new FluidRule());
    }

    protected FluidTransferRuleset(List<FluidRule> rules) {
        this.rules = new ObjectArrayList<>(rules);
        this.dist = Dist.DEDICATED_SERVER;
    }

    public void setChangeListener(@NotNull Runnable changeListener) {
        this.changeListener = changeListener;
    }

    public @NotNull FluidRuleAccess ruleAt(int index) {
        if (dist.isClient()) {
            return new ProtectedRuleAccess(rules.get(index), index);
        }
        return rules.get(index);
    }

    protected @NotNull FluidRule actualRuleAt(int index) {
        return rules.get(index);
    }

    public int ruleCount() {
        return rules.size();
    }

    public void applyAction(int index, RuleAction action) {
        switch (action) {
            case SHIFT_LEFT -> {
                FluidRule replace = rules.set(index - 1, rules.get(index));
                rules.set(index, replace);
            }
            case SHIFT_RIGHT -> {
                FluidRule replace = rules.set(index + 1, rules.get(index));
                rules.set(index, replace);
            }
            case DELETE -> {
                rules.remove(index);
                if (rules.isEmpty()) {
                    rules.add(new FluidRule());
                }
            }
            case ADD -> {
                rules.add(index, new FluidRule());
            }
        }
        toSync.add(buf -> {
            buf.writeEnum(action);
            buf.writeVarInt(index);
        });
    }

    @Override
    @UnmodifiableView
    public @Nullable AxonAddress getMatchingAddress(@NotNull FluidStack stack, boolean incoming) {
        for (FluidRule rule : rules) {
            if (incoming && !rule.isMatchesIncoming()) continue;
            if (!incoming && !rule.isMatchesOutgoing()) continue;
            if (rule.matches(stack)) {
                return rule.getAddress();
            }
        }
        return null;
    }

    @Override
    public @NotNull Collection<AxonAddress> getAllPossibleAddresses() {
        return rules.stream().map(FluidRule::getAddress).toList();
    }

    @Override
    public @NotNull TransferRuleset createNew(Dist dist) {
        return new FluidTransferRuleset(dist);
    }

    @Override
    public @NotNull Consumer<TransferRuleset> syncAction(FriendlyByteBuf buf, Dist destination) {
        if (destination.isClient()) {
            int count = buf.readVarInt();
            List<FluidRule> overwrite = new ObjectArrayList<>();
            for (int i = 0; i < count; i++) {
                FluidRule rule = new FluidRule();
                rule.address.read(buf);
                rule.matchesIncoming = buf.readBoolean();
                rule.matchesOutgoing = buf.readBoolean();
                for (int j = 0; j < 9; j++) {
                    rule.matchStacks[j] = buf.readFluidStack();
                }
                rule.whitelist = buf.readBoolean();
                rule.matchNBT = buf.readBoolean();
                overwrite.add(rule);
            }
            return r -> {
                if (r instanceof FluidTransferRuleset fluid) {
                    fluid.rules.clear();
                    fluid.rules.addAll(overwrite);
                    fluid.changeListener.run();
                }
            };
        } else {
            int count = buf.readVarInt();
            final List<Consumer<FluidTransferRuleset>> actions = new ObjectArrayList<>(count);
            for (int i = 0; i < count; i++) {
                RuleAction action = buf.readEnum(RuleAction.class);
                int index = buf.readVarInt();
                if (action == RuleAction.MODIFY) {
                    var act = buf.readEnum(ProtectedRuleAccess.ModifyType.class);
                    switch (act) {
                        case ADDRESS -> {
                            AxonAddress address = new AxonAddress();
                            address.read(buf);
                            actions.add(r -> r.actualRuleAt(index).setAddress(address));
                        }
                        case INCOMING -> {
                            boolean incoming = buf.readBoolean();
                            actions.add(r -> r.actualRuleAt(index).setMatchesIncoming(incoming));
                        }
                        case OUTGOING -> {
                            boolean outgoing = buf.readBoolean();
                            actions.add(r -> r.actualRuleAt(index).setMatchesOutgoing(outgoing));
                        }
                        case STACK -> {
                            int ind = buf.readVarInt();
                            FluidStack stack = buf.readFluidStack();
                            actions.add(r -> r.actualRuleAt(index).setMatchStack(ind, stack));
                        }
                        case WHITELIST -> {
                            boolean whitelist = buf.readBoolean();
                            actions.add(r -> r.actualRuleAt(index).setWhitelist(whitelist));
                        }
                        case NBT -> {
                            boolean matchNBT = buf.readBoolean();
                            actions.add(r -> r.actualRuleAt(index).setMatchNBT(matchNBT));
                        }
                    }
                } else {
                    actions.add(r -> r.applyAction(index, action));
                }
            }
            return r -> {
                if (r instanceof FluidTransferRuleset fluid) {
                    for (Consumer<FluidTransferRuleset> a : actions) {
                        a.accept(fluid);
                    }
                    fluid.changeListener.run();
                }
            };
        }
    }

    @Override
    public @NotNull Consumer<FriendlyByteBuf> clientSyncData() {
        return buf -> {
            buf.writeVarInt(rules.size());
            for (int i = 0; i < rules.size(); i++) {
                FluidRule rule = rules.get(i);
                rule.address.write(buf);
                buf.writeBoolean(rule.matchesIncoming);
                buf.writeBoolean(rule.matchesOutgoing);
                for (int j = 0; j < 9; j++) {
                    buf.writeFluidStack(rule.matchStacks[j]);
                }
                buf.writeBoolean(rule.whitelist);
                buf.writeBoolean(rule.matchNBT);
            }
        };
    }

    @Override
    public @NotNull AxonType getType() {
        return AxonType.FLUID;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull Consumer<FriendlyByteBuf> serverSyncData(@NotNull RulesetWidget widget) {
        var list = toSync;
        toSync = new ObjectArrayList<>();
        return buf -> {
            buf.writeVarInt(list.size());
            for (Consumer<FriendlyByteBuf> action : list) {
                action.accept(buf);
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean hasPendingSync(@NotNull RulesetWidget widget) {
        return !toSync.isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public @NotNull RulesetWidget createWidget(AbstractConnectorScreen<?> parent, int x, int y) {
        return new FluidRulesetWidget(this, parent, x, y);
    }

    public class ProtectedRuleAccess implements FluidRuleAccess {
        protected final @NotNull FluidRule backer;
        protected final int index;

        public ProtectedRuleAccess(@NotNull FluidRule backer, int index) {
            this.backer = backer;
            this.index = index;
        }

        @Override
        public @NotNull AxonAddress getAddress() {
            return backer.getAddress();
        }

        @Override
        public void setAddress(@NotNull AxonAddress address) {
            backer.setAddress(address);
            toSync.add(buf -> {
                buf.writeEnum(RuleAction.MODIFY);
                buf.writeVarInt(index);
                buf.writeEnum(ModifyType.ADDRESS);
                address.write(buf);
            });
        }

        @Override
        public boolean isMatchesIncoming() {
            return backer.isMatchesIncoming();
        }

        @Override
        public void setMatchesIncoming(boolean matchesIncoming) {
            backer.setMatchesIncoming(matchesIncoming);
            toSync.add(buf -> {
                buf.writeEnum(RuleAction.MODIFY);
                buf.writeVarInt(index);
                buf.writeEnum(ModifyType.INCOMING);
                buf.writeBoolean(matchesIncoming);
            });
        }

        @Override
        public boolean isMatchesOutgoing() {
            return backer.isMatchesOutgoing();
        }

        @Override
        public void setMatchesOutgoing(boolean matchesOutgoing) {
            backer.setMatchesOutgoing(matchesOutgoing);
            toSync.add(buf -> {
                buf.writeEnum(RuleAction.MODIFY);
                buf.writeVarInt(index);
                buf.writeEnum(ModifyType.OUTGOING);
                buf.writeBoolean(matchesOutgoing);
            });
        }

        @Override
        public FluidStack getMatchStack(int index) {
            return backer.matchStacks[index];
        }

        @Override
        public void setMatchStack(int index, FluidStack stack) {
            backer.matchStacks[index] = stack;
            toSync.add(buf -> {
                buf.writeEnum(RuleAction.MODIFY);
                buf.writeVarInt(this.index);
                buf.writeEnum(ModifyType.STACK);
                buf.writeVarInt(index);
                buf.writeFluidStack(stack);
            });
        }

        @Override
        public boolean isWhitelist() {
            return backer.isWhitelist();
        }

        @Override
        public void setWhitelist(boolean whitelist) {
            backer.setWhitelist(whitelist);
            toSync.add(buf -> {
                buf.writeEnum(RuleAction.MODIFY);
                buf.writeVarInt(index);
                buf.writeEnum(ModifyType.WHITELIST);
                buf.writeBoolean(whitelist);
            });
        }

        @Override
        public boolean isMatchNBT() {
            return backer.isMatchNBT();
        }

        @Override
        public void setMatchNBT(boolean matchNBT) {
            backer.setMatchNBT(matchNBT);
            toSync.add(buf -> {
                buf.writeEnum(RuleAction.MODIFY);
                buf.writeVarInt(index);
                buf.writeEnum(ModifyType.NBT);
                buf.writeBoolean(matchNBT);
            });
        }

        public enum ModifyType {
            ADDRESS, INCOMING, OUTGOING, STACK, WHITELIST, NBT
        }
    }

}
