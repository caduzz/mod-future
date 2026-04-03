package net.caduzz.futuremod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Small solid sphere for Lapse (blue) / Reversal (red) fusion orbs. */
public final class FusionOrbRenderer<T extends Entity> extends EntityRenderer<T> {

    private final int cr;
    private final int cg;
    private final int cb;

    public FusionOrbRenderer(EntityRendererProvider.Context context, int cr, int cg, int cb) {
        super(context);
        this.shadowRadius = 0f;
        this.cr = cr;
        this.cg = cg;
        this.cb = cb;
    }

    @Override
    public net.minecraft.resources.ResourceLocation getTextureLocation(T entity) {
        return VoidOrbMesh.WHITE;
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double camX, double camY, double camZ) {
        AABB box = entity.getBoundingBox().inflate(8.0);
        return frustum.isVisible(box.move(-camX, -camY, -camZ));
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Vec3 cam = entityRenderDispatcher.camera.getPosition();
        double x = Mth.lerp(partialTick, entity.xo, entity.getX()) - cam.x;
        double y = Mth.lerp(partialTick, entity.yo, entity.getY()) - cam.y;
        double z = Mth.lerp(partialTick, entity.zo, entity.getZ()) - cam.z;

        float age = entity.tickCount + partialTick;
        float pulse = 0.55f + Mth.sin(age * 0.12f) * 0.05f;

        int light = VoidOrbMesh.fullBrightLight();
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(pulse, pulse, pulse);

        VertexConsumer consumer = VoidOrbMesh.cutoutNoCullBuffer(buffer);
        VoidOrbMesh.renderUnitSphere(poseStack, consumer, light, cr, cg, cb, 255);

        poseStack.popPose();
    }
}
