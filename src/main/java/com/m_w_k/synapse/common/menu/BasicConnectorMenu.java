package com.m_w_k.synapse.common.menu;

import com.m_w_k.synapse.api.connect.AxonAddress;
import com.m_w_k.synapse.common.block.AxonBlock;
import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.common.connect.LocalConnectorDevice;
import com.m_w_k.synapse.network.ClientboundBasicDeviceDataPacket;
import com.m_w_k.synapse.network.ServerboundSetConnectorIDPacket;
import com.m_w_k.synapse.network.ServerboundSetSelectedConnectorPacket;
import com.m_w_k.synapse.network.SynapsePacketHandler;
import com.m_w_k.synapse.registry.SynapseMenuRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class BasicConnectorMenu extends AbstractContainerMenu {
    protected static final int INV_SLOT_START = 0;
    protected static final int INV_SLOT_END = 27;
    protected static final int USE_ROW_SLOT_START = 27;
    protected static final int USE_ROW_SLOT_END = 36;

    protected final ContainerLevelAccess access;
    protected final IntFunction<String> deviceNames;

    protected final int deviceCount;

    protected @Nullable IAxonBlockEntity be;

    @OnlyIn(Dist.CLIENT)
    protected byte sync;
    @OnlyIn(Dist.CLIENT)
    protected BitSet activeDevices;
    @OnlyIn(Dist.CLIENT)
    protected int selectedDevice;
    @OnlyIn(Dist.CLIENT)
    protected short selectedID;
    @OnlyIn(Dist.CLIENT)
    protected AxonAddress selectedAddress;

    public BasicConnectorMenu(int containerID, Inventory playerInv, ContainerLevelAccess access, IntFunction<String> deviceNames,
                              int deviceCount) {
        super(SynapseMenuRegistry.BASIC_CONNECTOR.get(), containerID);
        this.deviceCount = deviceCount;
        this.deviceNames = deviceNames;

        this.access = access;
        int i = 36;
        int j = 137;

        for(int k = 0; k < 3; ++k) {
            for(int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInv, l + k * 9 + 9, i + l * 18, j + k * 18));
            }
        }

        for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInv, i1, i + i1 * 18, 195));
        }
    }

    public static BasicConnectorMenu of(int containerID, Inventory playerInv, IAxonBlockEntity be) {
        BasicConnectorMenu menu = new BasicConnectorMenu(containerID, playerInv, ContainerLevelAccess.create(be.getLevel(), be.getBlockPos()), be::getNameBySlot, be.getSlots());
        menu.be = be;
        return menu;
    }

    public static BasicConnectorMenu read(int containerID, Inventory playerInv, FriendlyByteBuf buf) {
        int slots = buf.readVarInt();
        String[] names = new String[slots];
        for (int i = 0; i < slots; i++) {
            int length = buf.readVarInt();
            names[i] = buf.readCharSequence(length, Charset.defaultCharset()).toString();
        }
        BasicConnectorMenu ret = new BasicConnectorMenu(containerID, playerInv, ContainerLevelAccess.NULL, i -> names[i], slots);
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

    public int getDeviceCount() {
        return deviceCount;
    }

    public IntFunction<String> getDeviceNames() {
        return deviceNames;
    }

    public @NotNull ItemStack quickMoveStack(@NotNull Player p_39051_, int p_39052_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_39052_);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (p_39052_ >= INV_SLOT_START && p_39052_ < INV_SLOT_END) {
                if (!this.moveItemStackTo(itemstack1, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (p_39052_ >= USE_ROW_SLOT_START && p_39052_ < USE_ROW_SLOT_END) {
                if (!this.moveItemStackTo(itemstack1, INV_SLOT_START, INV_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(p_39051_, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return access.evaluate((level, pos) -> level.getBlockState(pos).getBlock() instanceof AxonBlock
                && player.distanceToSqr((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D, true);
    }

    protected static BitSet evaluateActiveness(@Nullable IAxonBlockEntity be) {
        BitSet set = new BitSet();
        if (be != null) {
        for (int i = 0; i < be.getSlots(); i++) {
            set.set(i, be.slotIsActive(i));
        }
        }
        return set;
    }

    public void sendToClient(ServerPlayer player, int device) {
        if (be == null || be.getLevel() == null) return;
        BitSet active = evaluateActiveness(be);
        ClientboundBasicDeviceDataPacket packet = active.get(device) ?
                new ClientboundBasicDeviceDataPacket(active, device, be.getBySlot(device).ensureRegistered(be.getLevel()))
                : new ClientboundBasicDeviceDataPacket(active, device, null, (short) 0);
        SynapsePacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public void updateID(ServerPlayer player, int device, short id) {
        if (be == null) return;
        be.getBySlot(device).setAddressID(id);
        sendToClient(player, device);
    }

    @OnlyIn(Dist.CLIENT)
    public void onSync() {
        sync++;
    }

    @OnlyIn(Dist.CLIENT)
    public byte getSync() {
        return sync;
    }

    @OnlyIn(Dist.CLIENT)
    public void setActiveDevices(BitSet activeDevices) {
        this.activeDevices = activeDevices;
    }

    @OnlyIn(Dist.CLIENT)
    public void setSelectedDevice(int selectedDevice) {
        this.selectedDevice = selectedDevice;
    }

    @OnlyIn(Dist.CLIENT)
    public void sendSelectedDevice(int selectedDevice) {
        SynapsePacketHandler.INSTANCE.sendToServer(new ServerboundSetSelectedConnectorPacket(selectedDevice));
    }

    @OnlyIn(Dist.CLIENT)
    public BitSet getActiveDevices() {
        return activeDevices;
    }

    @OnlyIn(Dist.CLIENT)
    public int getSelectedDevice() {
        return selectedDevice;
    }

    @OnlyIn(Dist.CLIENT)
    public void setSelectedAddress(AxonAddress selectedAddress) {
        this.selectedAddress = selectedAddress;
    }

    @OnlyIn(Dist.CLIENT)
    public AxonAddress getSelectedAddress() {
        return selectedAddress;
    }

    @OnlyIn(Dist.CLIENT)
    public void setSelectedID(short selectedID) {
        this.selectedID = selectedID;
    }

    @OnlyIn(Dist.CLIENT)
    public short getSelectedID() {
        return selectedID;
    }

    @OnlyIn(Dist.CLIENT)
    public void sendSelectedID(short selectedID) {
        SynapsePacketHandler.INSTANCE.sendToServer(new ServerboundSetConnectorIDPacket(getSelectedDevice(), selectedID));
    }
}
