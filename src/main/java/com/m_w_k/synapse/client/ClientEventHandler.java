package com.m_w_k.synapse.client;


import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.client.renderer.TestAxonRenderer;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = SynapseMod.MODID)
public final class ClientEventHandler {

}
