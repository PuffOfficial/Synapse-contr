package com.m_w_k.synapse.network;

import com.m_w_k.synapse.api.connect.AxonAddress;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.connect.ConnectorLevel;
import com.m_w_k.synapse.api.connect.IDSetResult;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;

import java.util.BitSet;

public class ClientboundBasicDeviceDataPacket extends ClientboundDeviceSyncPacket {

    protected final int slot;
    protected final AxonAddress address;
    protected final ConnectorLevel level;

    protected final IDSetResult setResult;

    public ClientboundBasicDeviceDataPacket(BitSet activeDevices, int slot, LocalConnectorDevice device, IDSetResult setResult) {
        this(activeDevices, slot, device.getAddress(), device.level(), setResult);
    }

    public ClientboundBasicDeviceDataPacket(BitSet activeDevices, int slot, AxonAddress address, ConnectorLevel level, IDSetResult setResult) {
        super(activeDevices);
        this.slot = slot;
        this.address = address;
        this.level = level;
        this.setResult = setResult;
    }

    public ClientboundBasicDeviceDataPacket(FriendlyByteBuf buf) {
        super(buf);
        slot = buf.readVarInt();
        if (buf.readBoolean()) {
            address = new AxonAddress();
            address.read(buf);
            level = buf.readEnum(ConnectorLevel.class);
        } else {
            address = null;
            level = ConnectorLevel.RELAY;
        }
        setResult = buf.readEnum(IDSetResult.class);
    }

    public void encode(FriendlyByteBuf buf) {
        super.encode(buf);
        buf.writeVarInt(slot);
        buf.writeBoolean(address != null);
        if (address != null) {
            address.write(buf);
            buf.writeEnum(level);
        }
        buf.writeEnum(setResult);
    }

    public int getSlot() {
        return slot;
    }

    public AxonAddress getAddress() {
        return address;
    }

    public ConnectorLevel getLevel() {
        return level;
    }

    public short getId() {
        return address.getShort(level);
    }

    public IDSetResult getSetResult() {
        return setResult;
    }
}
