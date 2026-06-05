package com.redstonedev.josearenthere.init;

import com.redstonedev.josearenthere.JoseArentHere;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, JoseArentHere.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> JOSE_SPAWN_EGG =
            ITEMS.register("jose_spawn_egg", () -> new ForgeSpawnEggItem(
                    ModEntities.JOSE, 0x83695B, 0x4E3E37,
                    new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
}
