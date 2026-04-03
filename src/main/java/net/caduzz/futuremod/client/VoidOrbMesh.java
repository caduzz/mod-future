package net.caduzz.futuremod.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Malha esférica unitária compartilhada (satélites + void roxo). */
final class VoidOrbMesh {

    static final ResourceLocation WHITE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private VoidOrbMesh() {
    }

    static void renderUnitSphere(
            PoseStack poseStack,
            VertexConsumer consumer,
            int light,
            int cr,
            int cg,
            int cb,
            int ca) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();
        int stacks = 8;
        int slices = 16;
        for (int si = 0; si < stacks; si++) {
            float v0 = (float) si / stacks;
            float v1 = (float) (si + 1) / stacks;
            float phi0 = (float) (Math.PI * v0);
            float phi1 = (float) (Math.PI * v1);
            for (int ti = 0; ti < slices; ti++) {
                float u0 = (float) ti / slices;
                float u1 = (float) (ti + 1) / slices;
                float th0 = (float) (Mth.TWO_PI * u0);
                float th1 = (float) (Mth.TWO_PI * u1);

                float x00 = Mth.sin(phi0) * Mth.cos(th0);
                float y00 = Mth.cos(phi0);
                float z00 = Mth.sin(phi0) * Mth.sin(th0);

                float x01 = Mth.sin(phi0) * Mth.cos(th1);
                float y01 = Mth.cos(phi0);
                float z01 = Mth.sin(phi0) * Mth.sin(th1);

                float x10 = Mth.sin(phi1) * Mth.cos(th0);
                float y10 = Mth.cos(phi1);
                float z10 = Mth.sin(phi1) * Mth.sin(th0);

                float x11 = Mth.sin(phi1) * Mth.cos(th1);
                float y11 = Mth.cos(phi1);
                float z11 = Mth.sin(phi1) * Mth.sin(th1);

                quad(consumer, mat, pose, light, cr, cg, cb, ca, x00, y00, z00, x01, y01, z01, x11, y11, z11, x10, y10, z10);
            }
        }
    }

    static int fullBrightLight() {
        return LightTexture.pack(15, 15);
    }

    static VertexConsumer translucentBuffer(net.minecraft.client.renderer.MultiBufferSource buffer) {
        return buffer.getBuffer(RenderType.entityTranslucent(WHITE));
    }

    /** Cutout: draws reliably (Fabulous / translucency sort issues skip entityTranslucent). */
    static VertexConsumer cutoutNoCullBuffer(net.minecraft.client.renderer.MultiBufferSource buffer) {
        return buffer.getBuffer(RenderType.entityCutoutNoCull(WHITE));
    }

    private static void quad(
            VertexConsumer consumer,
            Matrix4f mat,
            PoseStack.Pose pose,
            int light,
            int cr,
            int cg,
            int cb,
            int ca,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3) {
        vertex(consumer, mat, pose, light, cr, cg, cb, ca, x0, y0, z0, x0, y0, z0);
        vertex(consumer, mat, pose, light, cr, cg, cb, ca, x1, y1, z1, x1, y1, z1);
        vertex(consumer, mat, pose, light, cr, cg, cb, ca, x2, y2, z2, x2, y2, z2);
        vertex(consumer, mat, pose, light, cr, cg, cb, ca, x3, y3, z3, x3, y3, z3);
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f mat,
            PoseStack.Pose pose,
            int light,
            int cr,
            int cg,
            int cb,
            int ca,
            float px,
            float py,
            float pz,
            float nx,
            float ny,
            float nz) {
        consumer.addVertex(mat, px, py, pz)
                .setColor(cr, cg, cb, ca)
                .setUv(0.5f, 0.5f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}
