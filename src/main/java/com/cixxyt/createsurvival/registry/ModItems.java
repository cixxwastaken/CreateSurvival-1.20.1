package com.cixxyt.createsurvival.registry;

import com.cixxyt.createsurvival.CreateSurvivalMod;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static void registerIntegration(IEventBus eventBus) {
        System.out.println("[CreateSurvival] Registering ModItems");

        // Example: register custom items, temperature modifiers, etc.
        // ColdSweatAPI.registerTemperatureEffect(...);
    }
}
