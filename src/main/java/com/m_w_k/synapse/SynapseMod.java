package com.m_w_k.synapse;

import com.m_w_k.synapse.client.gui.BasicConnectorScreen;
import com.m_w_k.synapse.client.renderer.TestAxonRenderer;
import com.m_w_k.synapse.data.SynapseLootTableGen;
import com.m_w_k.synapse.network.SynapsePacketHandler;
import com.m_w_k.synapse.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SynapseMod.MODID)
public class SynapseMod {
    public static final String MODID = "synapse";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SynapseMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        registries(modEventBus);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::clientSetup);
        SynapsePacketHandler.init();
    }

    private void registries(IEventBus bus) {
        SynapseBlockRegistry.init(bus);
        SynapseItemRegistry.init(bus);
        SynapseBlockEntityRegistry.init(bus);
        SynapseCreativeTabsRegistry.init(bus);
        SynapseMenuRegistry.init(bus);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.DISTRIBUTOR_BLOCK.get(), TestAxonRenderer::new);
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.DAS_BLOCK.get(), TestAxonRenderer::new);
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.ENDPOINT_BLOCK.get(), TestAxonRenderer::new);
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.RELAY_BLOCK.get(), TestAxonRenderer::new);
    }

    private void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        gen.addProvider(event.includeServer(), SynapseLootTableGen.INSTANCE);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                () -> MenuScreens.register(SynapseMenuRegistry.BASIC_CONNECTOR.get(), BasicConnectorScreen::new)
        );
    }

    public static ResourceLocation resLoc(String path) {
        return new ResourceLocation(MODID, path);
    }
}
