package com.m_w_k.synapse;

import com.m_w_k.synapse.client.renderer.TestAxonRenderer;
import com.m_w_k.synapse.data.SynapseLootTableGen;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import com.m_w_k.synapse.registry.SynapseBlockRegistry;
import com.m_w_k.synapse.registry.SynapseCreativeTabsRegistry;
import com.m_w_k.synapse.registry.SynapseItemRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
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
    }

    private void registries(IEventBus bus) {
        SynapseBlockRegistry.init(bus);
        SynapseItemRegistry.init(bus);
        SynapseBlockEntityRegistry.init(bus);
        SynapseCreativeTabsRegistry.init(bus);
    }

    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.DISTRIBUTOR_BLOCK.get(), TestAxonRenderer::new);
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.DAS_BLOCK.get(), TestAxonRenderer::new);
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.ENDPOINT_BLOCK.get(), TestAxonRenderer::new);
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.RELAY_BLOCK.get(), TestAxonRenderer::new);
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
        gen.addProvider(event.includeServer(), SynapseLootTableGen.INSTANCE);
    }

    public static ResourceLocation resLoc(String path) {
        return new ResourceLocation(MODID, path);
    }
}
