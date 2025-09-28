package com.m_w_k.synapse.common.connect;

import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectionType;
import com.m_w_k.synapse.common.item.AxonItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Connection data owned by a block entity
 */
public final class LocalAxonConnection extends AxonConnection {

    public static final Codec<LocalAxonConnection> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ForgeRegistries.ITEMS.getCodec().xmap(i -> (AxonItem) i, i -> i).fieldOf("item").forGetter(LocalAxonConnection::getItem),
                    Codec.INT.fieldOf("sourceSlot").forGetter(LocalAxonConnection::getSourceSlot),
                    Vec3.CODEC.fieldOf("sourceRenderOffset").forGetter(LocalAxonConnection::getSourceRenderOffset),
                    BlockPos.CODEC.fieldOf("targetPos").forGetter(LocalAxonConnection::getTargetPos),
                    Codec.INT.fieldOf("targetSlot").forGetter(LocalAxonConnection::getTargetSlot),
                    Vec3.CODEC.fieldOf("targetRenderOffset").forGetter(LocalAxonConnection::getTargetRenderOffset),
                    AxonType.CODEC.fieldOf("axonType").forGetter(AxonConnection::getAxonType),
                    CompoundTag.CODEC.fieldOf("data").forGetter(AxonConnection::getData),
                    Codec.INT.xmap(i -> ConnectionType.TYPES[i], ConnectionType::ordinal).fieldOf("connectionType").forGetter(AxonConnection::getConnectionType)
            ).apply(instance, LocalAxonConnection::new));

    private final @NotNull AxonItem item;

    private final int sourceSlot;
    private final Vec3 sourceRenderOffset;
    private final BlockPos targetPos;
    private final int targetSlot;
    private final Vec3 targetRenderOffset;

    public LocalAxonConnection(@NotNull AxonItem item, int sourceSlot, Vec3 sourceRenderOffset, BlockPos targetPos,
                               int targetSlot, Vec3 targetRenderOffset, AxonType axonType,
                               @Nullable ConnectionType connectionType) {
        super(axonType, connectionType);
        this.item = item;
        this.sourceSlot = sourceSlot;
        this.sourceRenderOffset = sourceRenderOffset;
        this.targetPos = targetPos;
        this.targetSlot = targetSlot;
        this.targetRenderOffset = targetRenderOffset;
    }

    private LocalAxonConnection(@NotNull AxonItem item, int sourceSlot, Vec3 sourceRenderOffset, BlockPos targetPos,
                                int targetSlot, Vec3 targetRenderOffset, AxonType axonType,
                                CompoundTag tag, @Nullable ConnectionType connectionType) {
        super(axonType, tag, connectionType);
        this.item = item;
        this.sourceSlot = sourceSlot;
        this.sourceRenderOffset = sourceRenderOffset;
        this.targetPos = targetPos;
        this.targetSlot = targetSlot;
        this.targetRenderOffset = targetRenderOffset;
    }

    public @NotNull AxonItem getItem() {
        return item;
    }

    public int getSourceSlot() {
        return sourceSlot;
    }

    public Vec3 getSourceRenderOffset() {
        return sourceRenderOffset;
    }

    public int getTargetSlot() {
        return targetSlot;
    }

    public Vec3 getTargetRenderOffset() {
        return targetRenderOffset;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }
}
