package com.m_w_k.synapse.registry;

import com.m_w_k.synapse.SynapseMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public abstract class SynapseCreativeTabsRegistry {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SynapseMod.MODID);

    public static final RegistryObject<CreativeModeTab> SYNAPSE_TAB = CREATIVE_MODE_TABS.register("synapse_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.synapse"))
            .withTabsBefore(CreativeModeTabs.REDSTONE_BLOCKS)
            .icon(() -> new ItemStack(SynapseBlockRegistry.DISTRIBUTOR_BLOCK_1.get().asItem()))
            .displayItems((parameters, output) -> {
                output.acceptAll(SynapseBlockRegistry.BLOCKS.getEntries().stream().map(r -> new ItemStack(r.get())).toList());
                output.acceptAll(SynapseItemRegistry.ITEMS.getEntries().stream().map(r -> new ItemStack(r.get())).toList());
            }).build());

    public static void init(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
