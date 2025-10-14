package com.cixxyt.createsurvival.compat.coldsweat;

import net.minecraftforge.eventbus.api.IEventBus;

public class ColdSweatCompat {

    public static void registerIntegration(IEventBus eventBus) {
        System.out.println("[CreateSurvival] Cold Sweat detected — enabling crossover features!");

        // Example: register custom items, temperature modifiers, etc.
        // ColdSweatAPI.registerTemperatureEffect(...);
    }
}
