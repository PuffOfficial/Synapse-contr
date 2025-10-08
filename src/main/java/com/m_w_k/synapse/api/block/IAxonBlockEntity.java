package com.m_w_k.synapse.api.block;

import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface IAxonBlockEntity extends ICapabilityProvider, IForgeBlockEntity {

    boolean isRemoved();

    @Nullable Level getLevel();

    @NotNull BlockPos getBlockPos();

    int getSlots();

    @NotNull LocalConnectorDevice getBySlot(int slot);

    default boolean slotIsActive(int slot) {
        return true;
    }

    @NotNull String getNameBySlot(int slot);

    default boolean allowsUpstream(int slot) {
        return slotIsActive(slot);
    }

    default boolean allowsDownstream(int slot) {
        return slotIsActive(slot);
    }
}
