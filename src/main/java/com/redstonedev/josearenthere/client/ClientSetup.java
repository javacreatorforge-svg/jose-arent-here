package com.redstonedev.josearenthere.client;

import com.redstonedev.josearenthere.JoseArentHere;
import com.redstonedev.josearenthere.client.overlay.JumpscareOverlay;
import com.redstonedev.josearenthere.client.overlay.JumpscareState;
import com.redstonedev.josearenthere.client.renderer.JoseRenderer;
import com.redstonedev.josearenthere.init.ModEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> EntityRenderers.register(ModEntities.JOSE.get(), JoseRenderer::new));
    }

    @Mod.EventBusSubscriber(modid = JoseArentHere.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(), "jose_jumpscare", JumpscareOverlay.INSTANCE);
        }
    }

    @Mod.EventBusSubscriber(modid = JoseArentHere.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBus {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) JumpscareState.clientTick();
        }
    }
}
