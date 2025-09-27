package com.m_w_k.synapse;

import com.m_w_k.synapse.client.renderer.TestAxonRenderer;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import com.m_w_k.synapse.registry.SynapseBlockRegistry;
import com.m_w_k.synapse.registry.SynapseCreativeTabsRegistry;
import com.m_w_k.synapse.registry.SynapseItemRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
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
    }

    private void registries(IEventBus bus) {
        SynapseBlockRegistry.init(bus);
        SynapseItemRegistry.init(bus);
        SynapseBlockEntityRegistry.init(bus);
        SynapseCreativeTabsRegistry.init(bus);
    }

    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.TEST_BLOCK.get(), TestAxonRenderer::new);
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput packOutput = gen.getPackOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();
    }
}
