package com.cixxyt.createsurvival.compat.coldsweat;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Archived compatibility shim.  All methods now return safe defaults because the standalone
 * survival systems supersede the old integration.  Leaving the class in place avoids breaking
 * binary compatibility while steering students toward {@code TemperatureSystem}.
 */
public final class ColdSweatCompat {
    private ColdSweatCompat() {
    }

    public static void init() {
    }

    public static boolean isLoaded() {
        return false;
    }

    public static boolean applyWarmth(Player player, double strength) {
        return false;
    }

    public static boolean applyCooling(Player player, double strength) {
        return false;
    }

    public static boolean applyComfort(Player player, double strength) {
        return false;
    }

    public static Component describeTemperature(Player player, Level level) {
        return Component.translatable("tooltip.createsurvival.temperature.comfort");
    }

    public static void applyAmbientWarmth(Level level, BlockPos origin, double strength, double radius) {
    }

    public static void applyAmbientCooling(Level level, BlockPos origin, double strength, double radius) {
    }
}
