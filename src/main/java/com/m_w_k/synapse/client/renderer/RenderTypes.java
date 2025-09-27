package com.m_w_k.synapse.client.renderer;

import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class RenderTypes {

//    public static final RenderType AXON = create("axon", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.TRIANGLE_STRIP, 256, RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LEASH_SHADER).setTextureState(NO_TEXTURE).setCullState(NO_CULL).setLightmapState(LIGHTMAP).createCompositeState(false));


    static RenderType create(String p_173210_, VertexFormat p_173211_, VertexFormat.Mode p_173212_, int p_173213_, RenderType.CompositeState p_173214_) {
        return RenderType.create(p_173210_, p_173211_, p_173212_, p_173213_, false, false, p_173214_);
    }
}
