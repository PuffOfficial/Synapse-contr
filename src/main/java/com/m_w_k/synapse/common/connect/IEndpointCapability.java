package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.block.ruleset.TransferRuleset;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IEndpointCapability extends INBTSerializable<CompoundTag> {
    @NotNull IEndpointCapability child(Direction facing);

    @Nullable TransferRuleset getRuleset();
}
