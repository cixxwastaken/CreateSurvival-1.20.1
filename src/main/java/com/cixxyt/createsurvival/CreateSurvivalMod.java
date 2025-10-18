package com.cixxyt.createsurvival;

import com.cixxyt.createsurvival.compat.coldsweat.ColdSweatCompat;
import com.cixxyt.createsurvival.compat.create.CreateCompat;
import com.cixxyt.createsurvival.compat.thirst.ThirstCompat;
import com.cixxyt.createsurvival.content.items.FlaskItem;
import com.cixxyt.createsurvival.registry.ModItems;
import com.cixxyt.createsurvival.registry.ModBlocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;



@Mod(CreateSurvivalMod.MODID)
public class CreateSurvivalMod {
    public static final String MODID = "createsurvival";

    @SuppressWarnings("removal")
    public CreateSurvivalMod() {
        // Create mod event bus — this must be inside the constructor
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register stuff here
         ModItems.register(modEventBus);
         ModBlocks.register(modEventBus);



        // Optional integration
        if (ModList.get().isLoaded("cold_sweat")) {
            ColdSweatCompat.registerIntegration(modEventBus);
        }
        if (ModList.get().isLoaded("create")) {
            CreateCompat.registerIntegration(modEventBus);
        }
        if (ModList.get().isLoaded("thirst")) {
            ThirstCompat.init();
        }
    }
}
