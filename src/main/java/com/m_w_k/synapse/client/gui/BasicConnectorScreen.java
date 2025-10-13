package com.m_w_k.synapse.client.gui;

import com.m_w_k.synapse.common.menu.BasicConnectorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BasicConnectorScreen extends AbstractConnectorScreen<BasicConnectorMenu> {

    public BasicConnectorScreen(BasicConnectorMenu menu, Inventory playerInventory, Component p_97743_) {
        super(menu, playerInventory, p_97743_);
    }
}
