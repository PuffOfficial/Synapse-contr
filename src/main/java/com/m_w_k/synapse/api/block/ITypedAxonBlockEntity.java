package com.m_w_k.synapse.api.block;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import org.jetbrains.annotations.NotNull;

public interface ITypedAxonBlockEntity extends IAxonBlockEntity {

    int getSlotForType(@NotNull AxonType type);

    default @NotNull LocalConnectorDevice getByType(@NotNull AxonType type) {
        return getBySlot(getSlotForType(type));
    }

    default @NotNull String getNameByType(@NotNull AxonType type) {
        return getNameBySlot(getSlotForType(type));
    }
}
