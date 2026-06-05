package com.redstonedev.josearenthere.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.redstonedev.josearenthere.JoseArentHere;
import com.redstonedev.josearenthere.entity.JoseEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** Renders Jose.exe as a flat image (billboard quad) that always faces the player - no 3D model. */
@OnlyIn(Dist.CLIENT)
public class JoseRenderer extends EntityRenderer<JoseEntity> {
    private static final ResourceLocation TEX =
            new ResourceLocation(JoseArentHere.MODID, "textures/entity/joseexe.png");
    private static final float HEIGHT = 3.2F;
    private static final float WIDTH = HEIGHT * (1740.0F / 2320.0F); // portrait aspect

    public JoseRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0F;
    }

    @Override
    public ResourceLocation getTextureLocation(JoseEntity entity) { return TEX; }

    @Override
    public void render(JoseEntity entity, float entityYaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight) {
        pose.pushPose();
        pose.translate(0.0D, HEIGHT / 2.0D, 0.0D);
        pose.mulPose(this.entityRenderDispatcher.cameraOrientation()); // face the camera
        pose.mulPose(Vector3f.YP.rotationDegrees(180.0F));

        Matrix4f mat = pose.last().pose();
        Matrix3f norm = pose.last().normal();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEX));
        int light = 0x00F000F0; // full bright - always clearly visible
        float hw = WIDTH / 2.0F, hh = HEIGHT / 2.0F;
        vertex(vc, mat, norm, -hw, -hh, 0.0F, 1.0F, light);
        vertex(vc, mat, norm,  hw, -hh, 1.0F, 1.0F, light);
        vertex(vc, mat, norm,  hw,  hh, 1.0F, 0.0F, light);
        vertex(vc, mat, norm, -hw,  hh, 0.0F, 0.0F, light);

        pose.popPose();
        super.render(entity, entityYaw, partialTick, pose, buffer, packedLight);
    }

    private void vertex(VertexConsumer vc, Matrix4f mat, Matrix3f norm,
                        float x, float y, float u, float v, int light) {
        vc.vertex(mat, x, y, 0.0F)
          .color(255, 255, 255, 255)
          .uv(u, v)
          .overlayCoords(OverlayTexture.NO_OVERLAY)
          .uv2(light)
          .normal(norm, 0.0F, 1.0F, 0.0F)
          .endVertex();
    }
}
