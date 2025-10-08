package com.m_w_k.synapse.registry;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.common.menu.BasicConnectorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class SynapseMenuRegistry {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, SynapseMod.MODID);

    public static final RegistryObject<MenuType<BasicConnectorMenu>> BASIC_CONNECTOR = MENUS.register("basic_connector", () -> IForgeMenuType.create(BasicConnectorMenu::read));

    public static void init(IEventBus bus) {
        MENUS.register(bus);
    }

}
