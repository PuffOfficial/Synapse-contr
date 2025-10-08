package com.m_w_k.synapse.common.block.entity;

import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.ITypedAxonBlockEntity;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class TypedAxonBlockEntity extends AxonBlockEntity implements ITypedAxonBlockEntity {

    public TypedAxonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ConnectorLevel level) {
        super(type, pos, state);
        for (AxonType typ : AxonDeviceDefinitions.STANDARD_INV) {
            devices.add(new LocalConnectorDevice(typ, level));
        }
    }

    @Override
    public @NotNull String getNameBySlot(int slot) {
        return AxonDeviceDefinitions.STANDARD_INV.get(slot).name();
    }

    @Override
    public int getSlotForType(@NotNull AxonType type) {
        return AxonDeviceDefinitions.standard(type);
    }
}
