package com.redstonedev.josearenthere.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redstonedev.josearenthere.JoseArentHere;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class JumpscareOverlay implements IGuiOverlay {
    public static final JumpscareOverlay INSTANCE = new JumpscareOverlay();
    private static final ResourceLocation IMG =
            new ResourceLocation(JoseArentHere.MODID, "textures/gui/jose_jumpscare.png");
    private static final float IMG_W = 1740.0F, IMG_H = 2320.0F;

    @Override
    public void render(net.minecraftforge.client.gui.overlay.ForgeGui gui, PoseStack pose,
                       float partialTick, int width, int height) {
        if (JumpscareState.ticksRemaining <= 0) return;
        GuiComponent.fill(pose, 0, 0, width, height, 0xFF808080); // GRAY background
        float scale = Math.min(width / IMG_W, height / IMG_H);
        float drawW = IMG_W * scale, drawH = IMG_H * scale;
        float x = (width - drawW) / 2.0F, y = (height - drawH) / 2.0F;
        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, IMG);
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1.0F);
        GuiComponent.blit(pose, 0, 0, 0, 0.0F, 0.0F, (int) IMG_W, (int) IMG_H, (int) IMG_W, (int) IMG_H);
        pose.popPose();
        RenderSystem.disableBlend();
    }
}
