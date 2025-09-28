package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public interface FacedAxonBlockEntity extends IAxonBlockEntity {

    int getSlotForFace(Direction face);

    default @NotNull LocalConnectorDevice getByFace(Direction face) {
        return getBySlot(getSlotForFace(face));
    }
}
