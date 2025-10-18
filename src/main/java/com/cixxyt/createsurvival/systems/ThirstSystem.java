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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.List;

/**
 * Lightweight thirst implementation that mirrors the educational tone of the mod.  We keep the
 * entire hydration loop internal, storing values in persistent player data and documenting each
 * step so students can follow along.
 */
public final class ThirstSystem {
    private static final String TAG_ROOT = CreateSurvivalMod.MODID + "_thirst";
    private static final String TAG_LEVEL = "Level";
    private static final String TAG_TIMER = "Timer";
    private static final int TICKS_PER_SIP = 20 * 30; // Lose one point every 30 seconds.
    private static final int DANGER_THRESHOLD = 4;

    public static final int MAX_THIRST = 20;

    private ThirstSystem() {
    }

    /** Restores hydration and awards small bonuses for high-purity drinks. */
    public static void drink(Player player, int thirstRestored, int purity) {
        if (player == null) {
            return;
        }
        int boostedAmount = Math.max(1, thirstRestored) + Math.max(0, purity);
        setThirst(player, Math.min(MAX_THIRST, getThirst(player) + boostedAmount));
        applyPurityEffects(player, purity);
    }

    /** Applies flavor effects to illustrate why purified water matters. */
    public static void applyPurityEffects(Player player, int purity) {
        if (player == null) {
            return;
        }
        if (purity <= 0) {
            // Murky water risks nausea so learners see why automation is worthwhile.
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 0));
            return;
        }
        int duration = 20 * (5 + purity * 5);
        int amplifier = purity >= 3 ? 1 : 0;
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, amplifier, true, true));
        if (purity >= 2) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, amplifier, true, true));
        }
    }

    /**
     * Builds a tooltip component describing the stored water purity.  Returning a component instead
     * of mutating a list keeps the call-site compact and easier to read.
     */
    public static Component createPurityTooltip(int purity) {
        MutableComponent base = Component.translatable(
                "item.createsurvival.clockwork_canteen.purity",
                purity
        ).withStyle(ChatFormatting.AQUA);
        if (purity <= 0) {
            base.withStyle(ChatFormatting.DARK_RED);
        } else if (purity == 1) {
            base.withStyle(ChatFormatting.GOLD);
        } else if (purity >= 3) {
            base.withStyle(ChatFormatting.DARK_AQUA);
        }
        return base;
    }

    /**
     * Simple gradient used by the durability bar of the canteen.  The math mirrors vanilla's RGB
     * interpolation so the UI feels familiar.
     */
    public static int getPurityColor(int purity) {
        float clamped = Mth.clamp(purity / 3.0F, 0.0F, 1.0F);
        int red = (int) Mth.lerp(clamped, 0x8B, 0x2F);
        int green = (int) Mth.lerp(clamped, 0x45, 0x9B);
        int blue = (int) Mth.lerp(clamped, 0x13, 0xFF);
        return (red << 16) | (green << 8) | blue;
    }

    /**
     * Inspects the surrounding block to estimate purity.  Students can expand this with biome checks
     * or Create machines later on.
     */
    public static int calculatePurity(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return 0;
        }
        FluidState state = level.getFluidState(pos);
        if (state.getType() == Fluids.WATER) {
            return state.isSource() ? 3 : 2;
        }
        if (state.getType() == Fluids.FLOWING_WATER) {
            return 1;
        }
        return 0;
    }

    /** Adds a quick-reference tooltip for hunger/thirst crossover items. */
    public static void appendHydrationTooltip(List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.createsurvival.thirst_system.hydration").withStyle(ChatFormatting.BLUE));
    }

    /** Returns the tracked hydration stored on the player. */
    public static int getThirst(Player player) {
        if (player == null) {
            return MAX_THIRST;
        }
        CompoundTag root = getOrCreateRoot(player);
        return Mth.clamp(root.getInt(TAG_LEVEL), 0, MAX_THIRST);
    }

    /** Explicit setter so other systems can award or drain hydration. */
    public static void setThirst(Player player, int value) {
        if (player == null) {
            return;
        }
        CompoundTag root = getOrCreateRoot(player);
        root.putInt(TAG_LEVEL, Mth.clamp(value, 0, MAX_THIRST));
    }

    private static CompoundTag getOrCreateRoot(Player player) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (!root.contains(TAG_ROOT)) {
            CompoundTag thirstData = new CompoundTag();
            thirstData.putInt(TAG_LEVEL, MAX_THIRST);
            thirstData.putInt(TAG_TIMER, 0);
            root.put(TAG_ROOT, thirstData);
            persistent.put(Player.PERSISTED_NBT_TAG, root);
            return thirstData;
        }
        CompoundTag thirstData = root.getCompound(TAG_ROOT);
        root.put(TAG_ROOT, thirstData);
        persistent.put(Player.PERSISTED_NBT_TAG, root);
        return thirstData;
    }

    /** Copies the stored hydration from the old player to the new clone on respawn. */
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
     * Event listener container so we can register the logic without resorting to static annotations.
     */
    public static final class PlayerHooks {
        @SubscribeEvent
        public void clone(PlayerEvent.Clone event) {
            if (event.isWasDeath()) {
                copyData(event.getOriginal(), event.getEntity());
            }
        }

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.END) {
                return;
            }
            Player player = event.player;
            CompoundTag root = getOrCreateRoot(player);
            int timer = root.getInt(TAG_TIMER) + 1;
            if (timer >= TICKS_PER_SIP) {
                timer = 0;
                int current = Math.max(0, getThirst(player) - 1);
                setThirst(player, current);
                if (current <= 0) {
                    // Using starvation damage mirrors vanilla hunger while reinforcing hydration.
                    player.hurt(player.damageSources().starve(), 1.0F);
                } else if (current <= DANGER_THRESHOLD) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 4, 0, true, true));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 4, 0, true, true));
                }
            }
            root.putInt(TAG_TIMER, timer);
        }
    }
}
