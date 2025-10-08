package com.m_w_k.synapse.network;

import com.m_w_k.synapse.common.menu.BasicConnectorMenu;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SynapseClientPacketHandler {

    static void handle(ClientboundDeviceSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        if (Minecraft.getInstance().player == null) return;
        if (Minecraft.getInstance().player.containerMenu instanceof BasicConnectorMenu menu) {
            menu.setActiveDevices(packet.getActiveDevices());
            menu.onSync();
        }
    }

    static void handle(ClientboundBasicDeviceDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        if (Minecraft.getInstance().player == null) return;
        if (Minecraft.getInstance().player.containerMenu instanceof BasicConnectorMenu menu) {
            menu.setActiveDevices(packet.getActiveDevices());
            menu.setSelectedDevice(packet.getSlot());
            menu.setSelectedAddress(packet.getAddress());
            menu.setSelectedID(packet.getId());
            menu.onSync();
        }
    }
}
