package com.m_w_k.synapse.client.gui;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.api.connect.AxonAddress;
import com.m_w_k.synapse.common.menu.BasicConnectorMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class BasicConnectorScreen extends AbstractContainerScreen<BasicConnectorMenu> {
    static final ResourceLocation TEX_LOCATION = SynapseMod.resLoc("textures/gui/container/basic_connector.png");

    protected DeviceListWidget deviceList;
    protected @Nullable DeviceListWidget.DeviceEntry selected;
    protected int deviceCount;
    protected final @NotNull BitSet filteredDevices = new BitSet();

    protected EditBox deviceSearch;
    private String lastFilterText = "";

    protected byte lastSync;

    protected String selectedAddress = "";
    protected EditBox addressConfig;
    protected String lastDeviceID;

    public BasicConnectorScreen(BasicConnectorMenu menu, Inventory playerInventory, Component p_97743_) {
        super(menu, playerInventory, p_97743_);
        this.imageWidth = 230;
        this.imageHeight = 219;
    }

    public void setSelected(@Nullable DeviceListWidget.DeviceEntry selected) {
        this.selected = selected == this.selected ? null : selected;
        if (this.selected != null) {
            getMenu().sendSelectedDevice(selected.getSlot());
            getMenu().setSelectedAddress(null);
        }
        updateSelectedDeviceScreen();
    }

    public @NotNull BitSet getFilteredDevices() {
        return filteredDevices;
    }

    protected void updateSelectedDeviceScreen() {
        if (selected != null && getMenu().getSelectedAddress() != null) {
            selectedAddress = getMenu().getSelectedAddress().toString();
            lastDeviceID = AxonAddress.toHex(getMenu().getSelectedID(), false);
            if (!addressConfig.getValue().isEmpty() && !lastDeviceID.equals(addressConfig.getValue())) {
                addressConfig.setValue(lastDeviceID);
            }
            addressConfig.setVisible(true);
        } else {
            selectedAddress = "";
            addressConfig.setValue("0");
            addressConfig.setVisible(false);
            addressConfig.setFocused(false);
        }
    }

    protected void renderSelectedDeviceScreen(@NotNull GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        if (selected != null) {
            graphics.drawString(getFontRenderer(), selectedAddress, adjX(85), adjY(12), 0xFFFFFF, false);
        }
    }

    @Override
    protected void init() {
        super.init();
        deviceCount = getMenu().getDeviceCount();
        filteredDevices.clear();
        filteredDevices.or(getMenu().getActiveDevices());

        deviceSearch = new EditBox(getFontRenderer(), adjX(8), adjY(120), 68, 14, Component.translatable("synapse.menu.search"));
        deviceSearch.setMaxLength(50);
        addRenderableWidget(deviceSearch);
        setInitialFocus(deviceSearch);
        addressConfig = new EditBox(getFontRenderer(), adjX(85), adjY(24), 34, 14, Component.translatable("synapse.menu.address_config"));
        addRenderableWidget(addressConfig);
        addressConfig.setValue("0");
        addressConfig.setMaxLength(4);
        addressConfig.setFilter(s -> s.chars().allMatch(c -> Character.digit(c, 16) != -1));
//        addressConfig.setFilter(s -> s.equals("**") || s.equals("*") ||
//                s.chars().allMatch(c -> Character.digit(c, 16) != -1));
        addressConfig.setVisible(false);
        deviceList = new DeviceListWidget(this, adjX(7), 64, adjY(8), adjY(115));
        addRenderableWidget(deviceList);
    }

    @Override
    protected void containerTick() {
        deviceSearch.tick();
        addressConfig.tick();
        deviceList.setSelected(selected);
        if (lastSync != getMenu().getSync()) {
            lastSync = getMenu().getSync();
            if (selected != null && getMenu().getSelectedDevice() != selected.getSlot()) {
                getMenu().sendSelectedDevice(selected.getSlot());
                getMenu().setSelectedAddress(null);
            }
            updateSelectedDeviceScreen();
        }
        if (!deviceSearch.getValue().equals(lastFilterText)) {
            lastFilterText = deviceSearch.getValue();
            this.filteredDevices.clear();
            this.filteredDevices.or(getMenu().getActiveDevices());
            for (int i = 0; i < deviceCount; i++) {
                if (!filteredDevices.get(i)) continue;
                this.filteredDevices.set(i, StringUtils.toLowerCase(getMenu().getDeviceNames().apply(i))
                        .contains(StringUtils.toLowerCase(lastFilterText)));
            }
            deviceList.refreshList();
            if (selected != null) {
                if (deviceList.children().stream().noneMatch(e -> Objects.equals(e.getDeviceName(), selected.getDeviceName()))) {
                    setSelected(null);
                }
            }
        }
        if (selected == null) return;
        if (!addressConfig.getValue().equals(lastDeviceID)) {
            lastDeviceID = addressConfig.getValue();
            if (!lastDeviceID.isEmpty()) {
                getMenu().sendSelectedID(AxonAddress.fromHex(lastDeviceID));
            }
        }
    }

    @Override
    public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
        if (p_97765_ == 256) {
            getMinecraftInstance().player.closeContainer();
        }
        boolean editBox = getFocused() instanceof EditBox box &&
                (box.keyPressed(p_97765_, p_97766_, p_97767_) || box.canConsumeInput());
        return editBox || super.keyPressed(p_97765_, p_97766_, p_97767_);
    }

    @Override
    public boolean mouseDragged(double p_97752_, double p_97753_, int p_97754_, double p_97755_, double p_97756_) {
        return deviceList.mouseDragged(p_97752_, p_97753_, p_97754_, p_97755_, p_97756_) ||
                super.mouseDragged(p_97752_, p_97753_, p_97754_, p_97755_, p_97756_);
    }

    @Override
    public void render(GuiGraphics p_283479_, int p_283661_, int p_281248_, float p_281886_) {
        super.render(p_283479_, p_283661_, p_281248_, p_281886_);
        renderTooltip(p_283479_, p_283661_, p_281248_);
    }

    protected int adjX(int x) {
        return x + (this.width - this.imageWidth) / 2;
    }

    protected int adjY(int y) {
        return y + (this.height - this.imageHeight) / 2;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        graphics.blit(TEX_LOCATION, adjX(0), adjY(0), 0, 0, this.imageWidth, this.imageHeight);
        renderSelectedDeviceScreen(graphics, partial, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {}

    public Font getFontRenderer() {
        return font;
    }

    public Minecraft getMinecraftInstance() {
        return minecraft;
    }
}
