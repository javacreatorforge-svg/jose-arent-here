package com.redstonedev.josearenthere.client.overlay;

import com.redstonedev.josearenthere.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class JumpscareState {
    private JumpscareState() {}
    public static volatile int ticksRemaining = 0;

    public static void trigger(int ticks) {
        ticksRemaining = ticks;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(ModSounds.JUMPSCARE.get(), 1.0F, 1.0F));
    }
    public static void clientTick() { if (ticksRemaining > 0) ticksRemaining--; }
}
