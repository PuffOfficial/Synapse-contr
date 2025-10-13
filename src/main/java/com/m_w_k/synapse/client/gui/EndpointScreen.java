package com.m_w_k.synapse.client.gui;

import com.m_w_k.synapse.api.block.ruleset.TransferRuleset;
import com.m_w_k.synapse.common.menu.EndpointMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EndpointScreen extends AbstractConnectorScreen<EndpointMenu> {

    protected @Nullable TransferRuleset lastRuleset;
    protected @Nullable RulesetWidget rulesetWidget;

    public EndpointScreen(EndpointMenu menu, Inventory playerInventory, Component p_97743_) {
        super(menu, playerInventory, p_97743_);
    }

    @Override
    protected void updateSelectedDeviceScreen() {
        super.updateSelectedDeviceScreen();
        if (selected != null) {
            if (getMenu().getSelectedRuleset() != lastRuleset) {
                lastRuleset = getMenu().getSelectedRuleset();
                if (rulesetWidget != null) {
                    removeWidget(rulesetWidget);
                    rulesetWidget = null;
                }
                if (lastRuleset != null) {
                    rulesetWidget = lastRuleset.createWidget(this, adjX(85), adjY(44));
                    addWidget(rulesetWidget);
                }
            }
            if (rulesetWidget != null) {
                rulesetWidget.updateSelectedDevice();
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        lastRuleset = null;
        if (rulesetWidget != null) removeWidget(rulesetWidget);
        rulesetWidget = null;
    }

    @Override
    protected void renderSelectedDeviceScreen(@NotNull GuiGraphics graphics, float partial, int mouseX, int mouseY) {
        super.renderSelectedDeviceScreen(graphics, partial, mouseX, mouseY);
        if (selected != null && rulesetWidget != null) {
            rulesetWidget.render(graphics, mouseX, mouseY, partial);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (rulesetWidget != null && lastRuleset != null) {
            rulesetWidget.containerTick();
            if (lastRuleset.hasPendingSync(rulesetWidget)) {
                getMenu().sendRulesetSync(lastRuleset.serverSyncData(rulesetWidget));
            }
        }
    }
}
