package com.redstonedev.josearenthere.init;

import com.redstonedev.josearenthere.JoseArentHere;
import com.redstonedev.josearenthere.entity.JoseEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, JoseArentHere.MODID);

    public static final RegistryObject<EntityType<JoseEntity>> JOSE =
            ENTITIES.register("jose", () -> EntityType.Builder
                    .<JoseEntity>of(JoseEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.6F).clientTrackingRange(20)
                    .build(new ResourceLocation(JoseArentHere.MODID, "jose").toString()));
}
