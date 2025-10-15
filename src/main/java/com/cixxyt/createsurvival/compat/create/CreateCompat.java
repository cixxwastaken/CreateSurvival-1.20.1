package com.cixxyt.createsurvival.compat.create;

import net.minecraftforge.eventbus.api.IEventBus;

public class CreateCompat {


    public static void registerIntegration(IEventBus eventBus) {
        System.out.println("[CreateSurvival] Create Mod detected — enabling crossover features!");

        // Example: register custom items, temperature modifiers, etc.
        // ColdSweatAPI.registerTemperatureEffect(...);
    }


}
