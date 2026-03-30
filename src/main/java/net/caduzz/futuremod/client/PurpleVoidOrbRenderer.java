package net.caduzz.futuremod.client;

import net.caduzz.futuremod.entity.PurpleVoidOrbEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/**
 * O orbe em si fica invisível; a bola roxa é feita por {@link net.minecraft.world.entity.Display.BlockDisplay}
 * no servidor, que viaja junto com as partículas do {@link PurpleVoidOrbEntity#clientTrail()}.
 */
public class PurpleVoidOrbRenderer extends EntityRenderer<PurpleVoidOrbEntity> {

    public PurpleVoidOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0f;
    }

    @Override
    public ResourceLocation getTextureLocation(PurpleVoidOrbEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }

    @Override
    public boolean shouldRender(PurpleVoidOrbEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        double pad = entity.getEffectRadius() + 2.0;
        AABB box = entity.getBoundingBox().inflate(pad);
        return frustum.isVisible(box.move(-camX, -camY, -camZ));
    }
}
