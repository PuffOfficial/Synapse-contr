package com.m_w_k.synapse.network;

import net.minecraft.network.FriendlyByteBuf;

public class ServerboundSetSelectedConnectorPacket {

    protected final int slot;

    public ServerboundSetSelectedConnectorPacket(int slot) {
        this.slot = slot;
    }

    public ServerboundSetSelectedConnectorPacket(FriendlyByteBuf buf) {
        this.slot = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(slot);
    }

    public int getSlot() {
        return slot;
    }
}
