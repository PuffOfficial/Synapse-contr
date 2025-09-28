package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface IAxonBlockEntity extends ICapabilityProvider, IForgeBlockEntity {

    boolean isRemoved();

    @Nullable
    Level getLevel();

    int getSlots();

    @NotNull LocalConnectorDevice getBySlot(int slot);
}
