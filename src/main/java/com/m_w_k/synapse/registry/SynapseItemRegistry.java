package com.m_w_k.synapse.registry;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.item.AxonItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public abstract class SynapseItemRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, SynapseMod.MODID);

    public static final RegistryObject<AxonItem> ENERGY_AXON = ITEMS.register("axon.energy",
            () -> new AxonItem(new Item.Properties(), AxonType.ENERGY));
    public static final RegistryObject<AxonItem> ITEM_AXON = ITEMS.register("axon.item",
            () -> new AxonItem(new Item.Properties(), AxonType.ITEM));
    public static final RegistryObject<AxonItem> FLUID_AXON = ITEMS.register("axon.fluid",
            () -> new AxonItem(new Item.Properties(), AxonType.FLUID));

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }
}
