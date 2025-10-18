package com.cixxyt.createsurvival.systems;

import com.cixxyt.createsurvival.CreateSurvivalMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * Body/world temperature simulator designed to be readable for students.  We track a normalized
 * body temperature alongside the ambient world temperature so Create contraptions can meaningfully
 * influence survival gameplay without leaning on external dependencies.
 */
public final class TemperatureSystem {
    private static final String TAG_ROOT = CreateSurvivalMod.MODID + "_temperature";
    private static final String TAG_BODY = "Body";
    private static final String TAG_WORLD = "World";
    private static final double COMFORT_BAND = 0.15D;
    private static final double EXTREME_THRESHOLD = 0.65D;
    private static final double CRITICAL_THRESHOLD = 0.85D;

    private TemperatureSystem() {
    }

    /** Nudges the player's internal temperature upward. */
    public static void applyWarmth(Player player, double strength) {
        adjustBodyTemperature(player, Math.abs(strength));
    }

    /** Nudges the player's internal temperature downward. */
    public static void applyCooling(Player player, double strength) {
        adjustBodyTemperature(player, -Math.abs(strength));
    }

    /** Moves the body temperature toward neutral, simulating comfort gear. */
    public static boolean applyComfort(Player player, double strength) {
        if (player == null) {
            return false;
        }
        double body = getBodyTemperature(player);
        double delta = body > 0 ? -Math.abs(strength) : Math.abs(strength);
        adjustBodyTemperature(player, delta);
        return true;
    }

    /** Human-readable summary for tools like the Surveyor Thermometer. */
    public static Component describeTemperature(Player player, Level level) {
        double body = getBodyTemperature(player);
        double world = level != null ? sampleWorldTemperature(level, player.blockPosition()) : 0.0D;
        MutableComponent readout = Component.translatable(
                "item.createsurvival.surveyor_thermometer.reading",
                String.format("%.2f", body),
                String.format("%.2f", world)
        ).withStyle(ChatFormatting.AQUA);
        if (Math.abs(body) < COMFORT_BAND) {
            readout.append(" ").append(Component.translatable("tooltip.createsurvival.temperature.comfort").withStyle(ChatFormatting.GREEN));
        } else if (body > 0) {
            readout.append(" ").append(Component.translatable("tooltip.createsurvival.temperature.hot").withStyle(ChatFormatting.RED));
        } else {
            readout.append(" ").append(Component.translatable("tooltip.createsurvival.temperature.cold").withStyle(ChatFormatting.BLUE));
        }
        return readout;
    }

    /** Applies a heating aura around the supplied position. */
    public static void applyAmbientWarmth(Level level, BlockPos origin, double strength, double radius) {
        if (level == null || origin == null) {
            return;
        }
        double squared = Math.max(1.0D, radius * radius);
        for (Player player : level.players()) {
            if (player.distanceToSqr(origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D) <= squared) {
                applyWarmth(player, strength);
            }
        }
    }

    /** Mirrors {@link #applyAmbientWarmth(Level, BlockPos, double, double)} but for cooling. */
    public static void applyAmbientCooling(Level level, BlockPos origin, double strength, double radius) {
        if (level == null || origin == null) {
            return;
        }
        double squared = Math.max(1.0D, radius * radius);
        for (Player player : level.players()) {
            if (player.distanceToSqr(origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D) <= squared) {
                applyCooling(player, strength);
            }
        }
    }

    /** Returns the tracked body temperature. */
    public static double getBodyTemperature(Player player) {
        if (player == null) {
            return 0.0D;
        }
        CompoundTag root = getOrCreateRoot(player);
        return Mth.clamp(root.getDouble(TAG_BODY), -1.0D, 1.0D);
    }

    private static void setBodyTemperature(Player player, double value) {
        if (player == null) {
            return;
        }
        CompoundTag root = getOrCreateRoot(player);
        root.putDouble(TAG_BODY, Mth.clamp(value, -1.0D, 1.0D));
    }

    private static void setWorldTemperature(Player player, double value) {
        if (player == null) {
            return;
        }
        CompoundTag root = getOrCreateRoot(player);
        root.putDouble(TAG_WORLD, Mth.clamp(value, -1.0D, 1.0D));
    }

    private static void adjustBodyTemperature(Player player, double delta) {
        if (player == null || delta == 0.0D) {
            return;
        }
        double current = getBodyTemperature(player);
        setBodyTemperature(player, current + delta);
    }

    private static CompoundTag getOrCreateRoot(Player player) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (!root.contains(TAG_ROOT)) {
            CompoundTag temperatureData = new CompoundTag();
            temperatureData.putDouble(TAG_BODY, 0.0D);
            temperatureData.putDouble(TAG_WORLD, 0.0D);
            root.put(TAG_ROOT, temperatureData);
            persistent.put(Player.PERSISTED_NBT_TAG, root);
            return temperatureData;
        }
        CompoundTag data = root.getCompound(TAG_ROOT);
        root.put(TAG_ROOT, data);
        persistent.put(Player.PERSISTED_NBT_TAG, root);
        return data;
    }

    private static void copyData(Player original, Player clone) {
        if (original == null || clone == null) {
            return;
        }
        CompoundTag oldRoot = getOrCreateRoot(original).copy();
        CompoundTag persistent = clone.getPersistentData();
        CompoundTag root = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        root.put(TAG_ROOT, oldRoot);
        persistent.put(Player.PERSISTED_NBT_TAG, root);
    }

    /**
     * Samples environmental factors around the player and compresses them into a -1..1 range.
     */
    public static double sampleWorldTemperature(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return 0.0D;
        }
        Biome biome = level.getBiome(pos).value();
        double biomeTemp = biome.getBaseTemperature();
        double normalized = (biomeTemp - 0.5D) * 2.0D;
        double light = level.getBrightness(LightLayer.BLOCK, pos) / 15.0D;
        double sky = level.getBrightness(LightLayer.SKY, pos) / 15.0D;
        double dayFactor = level.isDay() ? 0.1D : -0.1D;
        double altitude = Mth.clamp((pos.getY() - level.getSeaLevel()) / 64.0D, -0.5D, 0.5D);
        return Mth.clamp(normalized + (light * 0.3D) + (sky * 0.2D) + dayFactor - altitude * 0.25D, -1.0D, 1.0D);
    }

    /**
     * Mirrors {@link ThirstSystem#handlePlayerClone(PlayerEvent.Clone)} so both systems preserve
     * their state across respawns.  Students can compare the two implementations to reinforce their
     * understanding of persistent NBT.
     */
    public static void handlePlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            copyData(event.getOriginal(), event.getEntity());
        }
    }

    /**
     * Server tick hook that samples the environment, drifts body temperature toward equilibrium, and
     * applies gameplay consequences.  Keeping the method static underlines that no per-player objects
     * are required for deterministic survival logic.
     */
    public static void handlePlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        Level level = player.level();
        BlockPos pos = player.blockPosition();
        double worldTemp = sampleWorldTemperature(level, pos);
        setWorldTemperature(player, worldTemp);

        double body = getBodyTemperature(player);
        double towardWorld = Mth.lerp(0.05D, body, worldTemp);
        setBodyTemperature(player, towardWorld);

        // Provide tangible penalties or bonuses based on the new temperature values.
        if (Math.abs(towardWorld) <= COMFORT_BAND) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 40, 0, true, false));
        } else if (towardWorld > EXTREME_THRESHOLD) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, true, true));
        } else if (towardWorld < -EXTREME_THRESHOLD) {
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 1, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 40, 0, true, true));
        }

        if (towardWorld > CRITICAL_THRESHOLD) {
            player.setSecondsOnFire(1);
        } else if (towardWorld < -CRITICAL_THRESHOLD) {
            player.hurt(player.damageSources().freeze(), 1.0F);
        }
    }
}
