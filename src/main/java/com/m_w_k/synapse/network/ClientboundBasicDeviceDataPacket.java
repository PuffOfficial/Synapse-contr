package com.m_w_k.synapse.network;

import com.m_w_k.synapse.api.connect.AxonAddress;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;

import java.util.BitSet;

public class ClientboundBasicDeviceDataPacket extends ClientboundDeviceSyncPacket {

    protected final int slot;
    protected final AxonAddress address;
    protected final short id;

    public ClientboundBasicDeviceDataPacket(BitSet activeDevices, int slot, LocalConnectorDevice device) {
        this(activeDevices, slot, device.getAddress(), device.getAddressID());
    }

    public ClientboundBasicDeviceDataPacket(BitSet activeDevices, int slot, AxonAddress address, short id) {
        super(activeDevices);
        this.slot = slot;
        this.address = address;
        this.id = id;
    }

    public ClientboundBasicDeviceDataPacket(FriendlyByteBuf buf) {
        super(buf);
        slot = buf.readVarInt();
        if (buf.readBoolean()) {
            address = new AxonAddress();
            address.read(buf);
            id = buf.readShort();
        } else {
            address = null;
            id = 0;
        }
    }

    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeVarInt(slot);
        buf.writeBoolean(address != null);
        if (address != null) {
            address.write(buf);
            buf.writeShort(id);
        }
    }

    public int getSlot() {
        return slot;
    }

    public AxonAddress getAddress() {
        return address;
    }

    public short getId() {
        return id;
    }
}
