package com.cixxyt.createsurvival.compat.coldsweat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Reflection-based integration with the Cold Sweat mod.  The helper keeps track of whether Cold
 * Sweat is loaded and exposes safe wrappers that either call into the mod or fall back to vanilla
 * behavior.  Each method is carefully documented so readers can learn how to design optional
 * integrations without risking class-loading crashes when the dependency is missing.
 */
public final class ColdSweatCompat {
    private static boolean loaded;
    private static Class<?> tempHelperClass;
    private static Method addTemperatureMethod;
    private static Method getBodyTemperatureMethod;

    private ColdSweatCompat() {}

    /** Probes the classpath for Cold Sweat's temperature helper and caches the reflection handles. */
    public static void init() {
        loaded = false;
        tempHelperClass = null;
        addTemperatureMethod = null;
        getBodyTemperatureMethod = null;

        for (String helperName : Arrays.asList(
                "dev.momostudios.coldsweat.api.temperature.PlayerTempHelper",
                "dev.momostudios.coldsweat.api.PlayerTempHelper",
                "dev.momostudios.coldsweat.api.util.PlayerTempHelper")) {
            try {
                tempHelperClass = Class.forName(helperName);
                break;
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (tempHelperClass == null) {
            return;
        }

        try {
            addTemperatureMethod = tempHelperClass.getMethod("addTemperature", Player.class, double.class);
        } catch (NoSuchMethodException ignored) {
            addTemperatureMethod = null;
        }

        try {
            getBodyTemperatureMethod = tempHelperClass.getMethod("getBodyTemperature", Player.class);
        } catch (NoSuchMethodException ignored) {
            getBodyTemperatureMethod = null;
        }

        loaded = addTemperatureMethod != null || getBodyTemperatureMethod != null;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    /**
     * Attempts to gently warm the player.  Returns {@code true} when Cold Sweat handled the change.
     */
    public static boolean applyWarmth(Player player, double strength) {
        if (!loaded) {
            return false;
        }
        boolean applied = applyTemperatureDelta(player, Math.abs(strength));
        if (!applied) {
            applyEffect(player, findEffect("warm", "toasty", "insulation"), 20 * 30, 0);
        }
        return applied;
    }

    /** Attempts to gently cool the player. */
    public static boolean applyCooling(Player player, double strength) {
        if (!loaded) {
            return false;
        }
        boolean applied = applyTemperatureDelta(player, -Math.abs(strength));
        if (!applied) {
            applyEffect(player, findEffect("cool", "chilled", "soothed"), 20 * 30, 0);
        }
        return applied;
    }

    /**
     * Used by the Thermo Vest to nudge temperatures toward neutral values.  The boolean return
     * tells the caller whether the integration succeeded so it can decide whether to apply a
     * fallback effect.
     */
    public static boolean applyComfort(Player player, double strength) {
        if (!loaded) {
            return false;
        }
        double delta = strength;
        Double current = getBodyTemperature(player);
        if (current != null) {
            if (current > 0.1D) {
                delta = -Math.abs(strength);
            } else if (current < -0.1D) {
                delta = Math.abs(strength);
            }
        }
        boolean warmed = applyTemperatureDelta(player, delta);
        if (!warmed) {
            applyEffect(player, findEffect("comfort", "insulation", "soothed"), 20 * 10, 0);
        }
        return warmed;
    }

    /**
     * Builds a tooltip or chat component summarizing the player's current temperature.
     * Cold Sweat provides more nuance, so we try to query it first.  When that fails we fall back to
     * vanilla biome temperature readings so the item remains informative without the dependency.
     */
    public static Component describeTemperature(Player player, Level level) {
        if (loaded && player != null) {
            Double bodyTemperature = getBodyTemperature(player);
            if (bodyTemperature != null) {
                return Component.translatable("item.createsurvival.surveyor_thermometer.reading.coldsweat",
                        Component.literal(String.format("%.2f", bodyTemperature)).withStyle(ChatFormatting.AQUA));
            }
        }

        float biomeTemp = level != null ? level.getBiome(player.blockPosition()).value().getBaseTemperature() : 0.5F;
        MutableComponent descriptor = Component.translatable("item.createsurvival.surveyor_thermometer.reading.vanilla",
                Component.literal(String.format("%.2f", biomeTemp)).withStyle(ChatFormatting.GOLD));
        if (biomeTemp < 0.15F) {
            descriptor.append(Component.literal(" ").append(Component.translatable("tooltip.createsurvival.temperature.cold")).withStyle(ChatFormatting.BLUE));
        } else if (biomeTemp > 1.0F) {
            descriptor.append(Component.literal(" ").append(Component.translatable("tooltip.createsurvival.temperature.hot")).withStyle(ChatFormatting.RED));
        }
        return descriptor;
    }

    private static boolean applyTemperatureDelta(Player player, double delta) {
        if (player == null || addTemperatureMethod == null) {
            return false;
        }
        try {
            Object result = addTemperatureMethod.invoke(null, player, delta);
            if (result instanceof Boolean booleanResult) {
                return booleanResult;
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static Double getBodyTemperature(Player player) {
        if (player == null || getBodyTemperatureMethod == null) {
            return null;
        }
        try {
            Object result = getBodyTemperatureMethod.invoke(null, player);
            if (result instanceof Number number) {
                return number.doubleValue();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static void applyEffect(Player player, MobEffect effect, int duration, int amplifier) {
        if (player == null || effect == null) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, true));
    }

    private static MobEffect findEffect(String... keywords) {
        if (!loaded) {
            return null;
        }
        return ForgeRegistries.MOB_EFFECTS.getValues().stream()
                .filter(effect -> {
                    ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(effect);
                    if (key == null || !"cold_sweat".equals(key.getNamespace())) {
                        return false;
                    }
                    String path = key.getPath();
                    return Arrays.stream(keywords).anyMatch(path::contains);
                })
                .min(Comparator.comparing(effect -> ForgeRegistries.MOB_EFFECTS.getKey(effect).getPath()))
                .orElse(null);
    }
}
