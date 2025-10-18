package com.cixxyt.createsurvival;

import com.cixxyt.createsurvival.compat.coldsweat.ColdSweatCompat;
import com.cixxyt.createsurvival.compat.create.CreateCompat;
import com.cixxyt.createsurvival.compat.thirst.ThirstCompat;
import com.cixxyt.createsurvival.registry.ModBlocks;
import com.cixxyt.createsurvival.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Entry point for the Create: Survival mod.  Forge discovers this class thanks to the
 * {@link Mod} annotation and instantiates it during the mod-loading pipeline.
 * <p>
 * Inside the constructor we perform all registration so Forge can wire our custom content
 * into the appropriate registries.  While doing that we also peek at the active mod list so
 * that optional integrations only wake up when their partner mods are present.
 */
@Mod(CreateSurvivalMod.MODID)
public class CreateSurvivalMod {
    public static final String MODID = "createsurvival";

    public CreateSurvivalMod() {
        // The mod event bus is Forge's bulletin board for registration events.  We fetch it here
        // so every registry class can listen for its respective callbacks (items, blocks, etc.).
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Item and block registration is always safe because Forge ignores empty registries.
        // Having the calls here keeps our startup order deterministic and easy to follow.
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        // Optional integration bootstraps.  Each helper double-checks that the companion mod is
        // really available before touching any of its classes, preventing class-loading crashes
        // when Create: Survival is installed on its own.
        if (ModList.get().isLoaded("create")) {
            CreateCompat.registerIntegration(modEventBus);
        }
        if (ModList.get().isLoaded("cold_sweat")) {
            ColdSweatCompat.init();
        }
        if (ModList.get().isLoaded("thirst")) {
            ThirstCompat.init();
        }
    }
}
