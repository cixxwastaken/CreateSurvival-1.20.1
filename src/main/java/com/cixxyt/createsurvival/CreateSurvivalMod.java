package com.cixxyt.createsurvival;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CreateSurvivalMod.MODID)
public class CreateSurvivalMod {
    public static final String MODID = "createsurvival";

    @SuppressWarnings("removal")
    public CreateSurvivalMod() {
        // Create mod event bus — this must be inside the constructor
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register stuff here
        // ModItems.register(modEventBus);
        // ModBlocks.register(modEventBus);

        // Optional integration
        if (ModList.get().isLoaded("cold_sweat")) {
            com.cixxyt.createsurvival.compat.coldsweat.ColdSweatCompat.registerIntegration(modEventBus);
        }
    }
}
