package com.redstonedev.josearenthere;

import com.mojang.logging.LogUtils;
import com.redstonedev.josearenthere.client.ClientSetup;
import com.redstonedev.josearenthere.entity.JoseEntity;
import com.redstonedev.josearenthere.event.ForgeEvents;
import com.redstonedev.josearenthere.init.ModEntities;
import com.redstonedev.josearenthere.init.ModItems;
import com.redstonedev.josearenthere.init.ModSounds;
import com.redstonedev.josearenthere.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(JoseArentHere.MODID)
public class JoseArentHere {
    public static final String MODID = "jose_arent_here";
    public static final Logger LOGGER = LogUtils.getLogger();

    public JoseArentHere() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntities.ENTITIES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
        modBus.addListener(this::attributes);
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
    }

    private void commonSetup(final FMLCommonSetupEvent e) {
        e.enqueueWork(PacketHandler::register);
        LOGGER.info("Jose Aren't Here loaded");
    }
    private void clientSetup(final FMLClientSetupEvent e) { ClientSetup.onClientSetup(e); }
    private void attributes(final EntityAttributeCreationEvent e) {
        e.put(ModEntities.JOSE.get(), JoseEntity.createAttributes().build());
    }
}
