package com.m_w_k.synapse.network;

import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.ruleset.TransferRuleset;
import com.m_w_k.synapse.common.menu.BasicConnectorMenu;
import com.m_w_k.synapse.common.menu.EndpointMenu;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
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
            menu.setSelectedLevel(packet.getLevel());
            menu.setSelectedID(packet.getId());
            menu.setSetResult(packet.getSetResult());
            menu.onSync();
        }
    }

    static void handle(EndpointRulesetSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        if (Minecraft.getInstance().player == null) return;
        if (Minecraft.getInstance().player.containerMenu instanceof EndpointMenu menu) {
            if (menu.getSelectedRuleset() != null && menu.getSelectedRuleset().getType() == packet.getType()) {
                packet.syncAction().ifPresent(a -> a.accept(menu.getSelectedRuleset()));
            } else {
                TransferRuleset ruleset = AxonDeviceDefinitions.newEndpointRuleset(packet.getType(), Dist.CLIENT);
                packet.syncAction().ifPresent(a -> a.accept(ruleset));
                menu.setSelectedRuleset(ruleset);
            }
            menu.onSync();
        }
    }
}
