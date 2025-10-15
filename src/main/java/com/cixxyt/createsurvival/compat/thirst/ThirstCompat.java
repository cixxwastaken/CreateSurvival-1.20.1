package com.cixxyt.createsurvival.compat.thirst;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

public class ThirstCompat {

    public static void registerIntegration(IEventBus eventBus) {
        System.out.println("[CreateSurvival] Thirst Was Taken Mod detected — enabling crossover features!");

        // Example: register custom items, temperature modifiers, etc.
        // ColdSweatAPI.registerTemperatureEffect(...);
    }

    private static final String MODID = "thirst";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MODID);
    }

    // <-- THIS METHOD MUST EXIST
    public static void onDrink(Player player, ItemStack flask) {
        if (!isLoaded()) return;

        try {
            // Example reflection to call Thirst Was Taken API
            Class<?> thirstHelper = Class.forName("com.thirstmod.api.ThirstHelper");
            thirstHelper.getMethod("addThirst", Player.class, int.class, float.class)
                    .invoke(null, player, 2, 1.0F);
        } catch (Exception ignored) { }
    }
}


