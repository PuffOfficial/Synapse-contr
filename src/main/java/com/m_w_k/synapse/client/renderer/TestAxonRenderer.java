package com.m_w_k.synapse.client.renderer;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.block.entity.TestBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class TestAxonRenderer implements BlockEntityRenderer<TestBlockEntity> {

    private static final ResourceLocation testTex = new ResourceLocation(SynapseMod.MODID, "block/test_texture");
    private static final Vec3 UP = new Vec3(0, 1, 0);

    public TestAxonRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull TestBlockEntity be, float partialTicks, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int light, int overlay) {
        if (Minecraft.getInstance().getCameraEntity() == null || be.getLevel() == null) return;
        pose.pushPose();
        BlockPos pos = be.getBlockPos();
        Vec3 testSource = be.getBlockPos().getCenter();
        pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        Vec3 testTarget = be.getBlockPos().north(15).west(5).above(5).getCenter();
        int points = 1 + (int) (curveLength(testSource, testTarget) * 2);
        Vec3[] ropePoints = new Vec3[points];
        for (int i = 0; i < points; i++) {
            double lerp = (double) i / points;
            double ylerp = testTarget.y > testSource.y ? lerp : (lerp - 1);
            ropePoints[i] = new Vec3(testSource.x + lerp * (testTarget.x - testSource.x),
                    testSource.y + ylerp * ylerp * (testTarget.y - testSource.y),
                    testSource.z + lerp * (testTarget.z - testSource.z));
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(testTex);
        float minU = sprite.getU(0);
        float maxU = sprite.getU(16);
//        Tesselator tesselator = RenderSystem.renderThreadTesselator();
//        BufferBuilder bufferbuilder = tesselator.getBuilder();
//        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.solid());
//        Vec3 camera = Minecraft.getInstance().getCameraEntity().getEyePosition(partialTicks);
        for (int i = 0; i < points - 1; i++) {
            Vec3 start = ropePoints[i];
            Vec3 end = ropePoints[i + 1];

            Vec3 midpoint = start.lerp(end, 0.5);
            int sectionLight = LevelRenderer.getLightColor(be.getLevel(), BlockPos.containing(midpoint.x, midpoint.y, midpoint.z));
//            Vec3 cam = camera.subtract(midpoint).normalize();

            Vec3 rope = end.subtract(start);
            Vec3 ropePrev = i > 0 ? start.subtract(ropePoints[i - 1]).add(rope) : rope;
            Vec3 ropeNext = i < points - 2 ? ropePoints[i + 2].subtract(end).add(rope) : rope;
            box(buffer, pose, start, end, ropeNext, ropePrev, sprite, i, sectionLight, overlay);
        }
        pose.popPose();
    }

    private double curveLength(Vec3 source, Vec3 target) {
        double dx = Math.abs(target.x - source.x);
        double dy = Math.abs(target.y - source.y);
        double dz = Math.abs(target.z - source.z);

        // position x/z = source + t * (target - source)
        // position y = source + t^2 * (target - source) (or (t-1)^2)
        // velocity x/z = target - source
        // velocity y = 2t * (target - source) (or 2(t-1))
        // magnitude squared = (dx)^2 + 4(t^2)(dy)^2 + (dz)^2
        // magnitude = sqrt(dx^2 + 4t^2*(dy)^2 + dz^2)

        // integral from 0 to 1
        double dxz = dx + dz;
        double sqrtDy = Math.sqrt(dy);
        double sqrt4 = Math.sqrt(dx + 4 * dy + dz);
        return dxz * (Math.log((sqrt4 + 2 * sqrtDy) / Math.sqrt(dxz))) / (4 * sqrtDy) + sqrt4 / 2;
    }

    private static void box(VertexConsumer b, PoseStack mat, Vec3 start, Vec3 end, Vec3 ropeNext, Vec3 ropePrev, TextureAtlasSprite sprite, int position, int light, int overlay) {
        Vec3 rope = end.subtract(start);
        Vec3 width = UP.cross(rope);
        Vec3 widthStart = UP.cross(ropePrev);
        Vec3 widthEnd = UP.cross(ropeNext);
        Vec3 height;
        Vec3 heightStart;
        Vec3 heightEnd;
        if (width.lengthSqr() < 1.0E-8D) {
            width = new Vec3(1, 0, 0);
            height = new Vec3(0, 0, 1);
        } else {
            height = rope.cross(width);
        }
        if (widthStart.lengthSqr() < 1.0E-8D) {
            widthStart = new Vec3(1, 0, 0);
            heightStart = new Vec3(0, 0, 1);
        } else {
            heightStart = ropePrev.cross(widthStart);
        }
        if (widthEnd.lengthSqr() < 1.0E-8D) {
            widthEnd = new Vec3(1, 0, 0);
            heightEnd = new Vec3(0, 0, 1);
        } else {
            heightEnd = ropeNext.cross(widthEnd);
        }
        width = width.normalize().scale(0.15625);
        widthStart = widthStart.normalize().scale(0.15625);
        widthEnd = widthEnd.normalize().scale(0.15625);
        height = height.normalize().scale(0.15625);
        heightStart = heightStart.normalize().scale(0.15625);
        heightEnd = heightEnd.normalize().scale(0.15625);
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV(((16 * position) % 128) / 8f);
        float maxV = sprite.getV(((16 * position) % 128 + 16) / 8f);
        // top
        vertex(b, mat, start.add(widthStart).add(heightStart), maxU, minV, height, light, overlay);
        vertex(b, mat, start.subtract(widthStart).add(heightStart), minU, minV, height, light, overlay);
        vertex(b, mat, end.subtract(widthEnd).add(heightEnd), minU, maxV, height, light, overlay);
        vertex(b, mat, end.add(widthEnd).add(heightEnd), maxU, maxV, height, light, overlay);
        // bottom
        Vec3 v = height.scale(-1);
        vertex(b, mat, start.subtract(widthStart).subtract(heightStart), minU, minV, v, light, overlay);
        vertex(b, mat, start.add(widthStart).subtract(heightStart), maxU, minV, v, light, overlay);
        vertex(b, mat, end.add(widthEnd).subtract(heightEnd), maxU, maxV, v, light, overlay);
        vertex(b, mat, end.subtract(widthEnd).subtract(heightEnd), minU, maxV, v, light, overlay);
        // left
        vertex(b, mat, start.subtract(heightStart).add(widthStart), minU, minV, width, light, overlay);
        vertex(b, mat, start.add(heightStart).add(widthStart), maxU, minV, width, light, overlay);
        vertex(b, mat, end.add(heightEnd).add(widthEnd), maxU, maxV, width, light, overlay);
        vertex(b, mat, end.subtract(heightEnd).add(widthEnd), minU, maxV, width, light, overlay);
        // right
        v = width.scale(-1);
        vertex(b, mat, start.add(heightStart).subtract(widthStart), maxU, minV, v, light, overlay);
        vertex(b, mat, start.subtract(heightStart).subtract(widthStart), minU, minV, v, light, overlay);
        vertex(b, mat, end.subtract(heightEnd).subtract(widthEnd), minU, maxV, v, light, overlay);
        vertex(b, mat, end.add(heightEnd).subtract(widthEnd), maxU, maxV, v, light, overlay);
    }

    private static void vertex(VertexConsumer b, PoseStack mat, Vec3 pos, float u, float v, Vec3 normal, int light, int overlay) {
        b.vertex(mat.last().pose(), (float) pos.x, (float) pos.y, (float) pos.z)
                .color(1F, 1F, 1F, 1F)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(mat.last().normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }
}
