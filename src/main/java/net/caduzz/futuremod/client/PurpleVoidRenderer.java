package net.caduzz.futuremod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.caduzz.futuremod.entity.PurpleVoidEntity;
import net.caduzz.futuremod.purplevoid.PurpleVoidPhase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Malha esférica roxa estável; escala e ease vêm dos dados sincronizados + {@code partialTick}. */
public class PurpleVoidRenderer extends EntityRenderer<PurpleVoidEntity> {

    public PurpleVoidRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0f;
    }

    @Override
    public ResourceLocation getTextureLocation(PurpleVoidEntity entity) {
        return VoidOrbMesh.WHITE;
    }

    @Override
    public boolean shouldRender(PurpleVoidEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        float r = Math.max(2.0f, entity.getSyncedVisualRadius());
        AABB box = entity.getBoundingBox().inflate(Math.max(r * 4.0, 6.0));
        return frustum.isVisible(box.move(-camX, -camY, -camZ));
    }

    @Override
    public void render(
            PurpleVoidEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight) {
        Vec3 cam = entityRenderDispatcher.camera.getPosition();
        double x = Mth.lerp(partialTick, entity.xo, entity.getX()) - cam.x;
        double y = Mth.lerp(partialTick, entity.yo, entity.getY()) - cam.y;
        double z = Mth.lerp(partialTick, entity.zo, entity.getZ()) - cam.z;

        float rBlocks = entity.getSyncedVisualRadius();
        int ticks = entity.getSyncedTicksAlive();
        int popMax = Math.max(1, entity.getPopTicksMax());
        PurpleVoidPhase phase = entity.getPhase();

        float rawEase =
                phase == PurpleVoidPhase.FUSION_POP ? Mth.clamp((ticks + partialTick) / popMax, 0.0f, 1.0f) : 1.0f;
        float popEase = phase == PurpleVoidPhase.FUSION_POP ? Math.max(0.3f, rawEase) : 1.0f;
        float age = ticks + partialTick;
        float pulse = 1.0f + Mth.sin(age * 0.11f) * 0.06f;
        float worldRadius = Math.max(0.45f, rBlocks * pulse * popEase);

        int light = VoidOrbMesh.fullBrightLight();
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.scale(worldRadius, worldRadius, worldRadius);

        VertexConsumer consumer = VoidOrbMesh.cutoutNoCullBuffer(buffer);
        VoidOrbMesh.renderUnitSphere(poseStack, consumer, light, 180, 55, 255, 230);

        poseStack.popPose();
    }
}
