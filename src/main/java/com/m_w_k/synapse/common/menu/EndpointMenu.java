package com.m_w_k.synapse.common.menu;

import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.block.ruleset.TransferRuleset;
import com.m_w_k.synapse.api.connect.IDSetResult;
import com.m_w_k.synapse.common.block.entity.EndpointBlockEntity;
import com.m_w_k.synapse.network.EndpointRulesetSyncPacket;
import com.m_w_k.synapse.network.SynapsePacketHandler;
import com.m_w_k.synapse.registry.SynapseMenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class EndpointMenu extends BasicConnectorMenu {

    @OnlyIn(Dist.CLIENT)
    protected TransferRuleset selectedRuleset;

    public EndpointMenu(int containerID, Inventory playerInv, ContainerLevelAccess access, IntFunction<String> deviceNames, int deviceCount) {
        super(SynapseMenuRegistry.ENDPOINT.get(), containerID, playerInv, access, deviceNames, deviceCount);
    }

    public static EndpointMenu of(int containerID, Inventory playerInv, IAxonBlockEntity be) {
        EndpointMenu menu = new EndpointMenu(containerID, playerInv, ContainerLevelAccess.create(be.getLevel(), be.getBlockPos()), be::getNameBySlot, be.getSlots());
        menu.be = be;
        return menu;
    }

    public static EndpointMenu read(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        int slots = buf.readVarInt();
        String[] names = new String[slots];
        for (int i = 0; i < slots; i++) {
            int length = buf.readVarInt();
            names[i] = buf.readCharSequence(length, Charset.defaultCharset()).toString();
        }
        EndpointMenu ret = new EndpointMenu(containerID, playerInv, ContainerLevelAccess.NULL, i -> names[i], slots);
        ret.setActiveDevices(buf.readBitSet());
        return ret;
    }

    public static Consumer<FriendlyByteBuf> writer(IAxonBlockEntity be) {
        return buf -> {
            buf.writeVarInt(be.getSlots());
            for (int i = 0; i < be.getSlots(); i++) {
                String name = be.getNameBySlot(i);
                buf.writeVarInt(name.length());
                buf.writeCharSequence(name, Charset.defaultCharset());
            }
            buf.writeBitSet(evaluateActiveness(be));
        };
    }

    @Override
    public void sendToClient(ServerPlayer player, int device, IDSetResult result) {
        super.sendToClient(player, device, result);
        TransferRuleset ruleset = getRulesetServerside(device);
        if (ruleset == null) return;
        SynapsePacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new EndpointRulesetSyncPacket(ruleset.getType(), ruleset.clientSyncData(), Dist.CLIENT, device));
    }

    public TransferRuleset getRulesetServerside(int device) {
        if (be == null || be.getLevel() == null || !(be instanceof EndpointBlockEntity endpoint)) return null;
        if (be.slotIsActive(device)) {
            return endpoint.rulesetForSlot(device);
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setSelectedDevice(int selectedDevice) {
        super.setSelectedDevice(selectedDevice);
        setSelectedRuleset(null);
    }

    @OnlyIn(Dist.CLIENT)
    public void setSelectedRuleset(TransferRuleset selectedRuleset) {
        this.selectedRuleset = selectedRuleset;
    }

    @OnlyIn(Dist.CLIENT)
    public TransferRuleset getSelectedRuleset() {
        return selectedRuleset;
    }

    @OnlyIn(Dist.CLIENT)
    public void sendRulesetSync(Consumer<FriendlyByteBuf> sync) {
        TransferRuleset ruleset = getSelectedRuleset();
        if (ruleset == null) return;
        SynapsePacketHandler.INSTANCE.sendToServer(new EndpointRulesetSyncPacket(ruleset.getType(), sync, Dist.DEDICATED_SERVER, selectedDevice));
    }
}
