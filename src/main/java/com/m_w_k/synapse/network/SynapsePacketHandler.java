package com.m_w_k.synapse.network;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.common.menu.BasicConnectorMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class SynapsePacketHandler {
    public static final String VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(SynapseMod.resLoc("main"),
            () -> VERSION, VERSION::equals, VERSION::equals);

    private static int ids;

    private SynapsePacketHandler() {}

    public static void init() {
        INSTANCE.registerMessage(ids++, ServerboundSetSelectedConnectorPacket.class,
                ServerboundSetSelectedConnectorPacket::encode, ServerboundSetSelectedConnectorPacket::new,
                SynapsePacketHandler::handle);
        INSTANCE.registerMessage(ids++, ServerboundSetConnectorIDPacket.class,
                ServerboundSetConnectorIDPacket::encode, ServerboundSetConnectorIDPacket::new,
                SynapsePacketHandler::handle);
        INSTANCE.registerMessage(ids++, ClientboundDeviceSyncPacket.class,
                ClientboundDeviceSyncPacket::encode, ClientboundDeviceSyncPacket::new,
                SynapsePacketHandler::handle);
        INSTANCE.registerMessage(ids++, ClientboundBasicDeviceDataPacket.class,
                ClientboundBasicDeviceDataPacket::encode, ClientboundBasicDeviceDataPacket::new,
                SynapsePacketHandler::handle);
    }

    private static void handle(ServerboundSetSelectedConnectorPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;
            if (sender.containerMenu instanceof BasicConnectorMenu menu) {
                menu.sendToClient(sender, packet.getSlot());
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handle(ServerboundSetConnectorIDPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;
            if (sender.containerMenu instanceof BasicConnectorMenu menu) {
                menu.updateID(sender, packet.getSlot(), packet.getId());
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handle(ClientboundDeviceSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                // Make sure it's only executed on the physical client
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SynapseClientPacketHandler.handle(packet, ctx))
        );
        ctx.get().setPacketHandled(true);
    }

    private static void handle(ClientboundBasicDeviceDataPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                // Make sure it's only executed on the physical client
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SynapseClientPacketHandler.handle(packet, ctx))
        );
        ctx.get().setPacketHandled(true);
    }
}
