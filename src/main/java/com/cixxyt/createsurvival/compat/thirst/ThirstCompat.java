package com.cixxyt.createsurvival.compat.thirst;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ThirstCompat {

    private static boolean loaded = false;
    private static Field playerThirstField;
    private static Method drinkMethod;
    private static Method getPurityMethod;
    private static Method getPurityTextMethod;
    private static Method getPurityColorMethod;
    private static Method addPurityByBlockMethod;
    private static Method addPurityByValueMethod;
    private static Method getBlockPurityMethod;
    private static Method givePurityEffectsMethod;
    private static Method givePurityEffectsStackMethod;

    public static boolean isLoaded() {
        return loaded;
    }

    public static void init() {
        clearReflection();
        try {
            Class<?> modCapsClass = Class.forName("dev.ghen.thirst.foundation.common.capability.ModCapabilities");
            playerThirstField = modCapsClass.getField("PLAYER_THIRST");

            Class<?> iThirstClass = Class.forName("dev.ghen.thirst.foundation.common.capability.IThirst");
            drinkMethod = iThirstClass.getMethod("drink", Player.class, int.class, int.class);

            Class<?> waterPurityClass = Class.forName("dev.ghen.thirst.content.purity.WaterPurity");
            getPurityMethod = waterPurityClass.getMethod("getPurity", ItemStack.class);
            try {
                getPurityTextMethod = waterPurityClass.getMethod("getPurityText", int.class);
            } catch (NoSuchMethodException ignored) {
                getPurityTextMethod = null;
            }
            getPurityColorMethod = waterPurityClass.getMethod("getPurityColor", int.class);
            try {
                addPurityByBlockMethod = waterPurityClass.getMethod("addPurity", ItemStack.class, BlockPos.class, Level.class);
            } catch (NoSuchMethodException ignored) {
                addPurityByBlockMethod = null;
            }
            try {
                addPurityByValueMethod = waterPurityClass.getMethod("addPurity", ItemStack.class, int.class);
            } catch (NoSuchMethodException ignored) {
                addPurityByValueMethod = null;
            }
            getBlockPurityMethod = waterPurityClass.getMethod("getBlockPurity", Level.class, BlockPos.class);
            givePurityEffectsMethod = waterPurityClass.getMethod("givePurityEffects", Player.class, int.class);
            try {
                givePurityEffectsStackMethod = waterPurityClass.getMethod("givePurityEffects", Player.class, ItemStack.class);
            } catch (NoSuchMethodException ignored) {
                givePurityEffectsStackMethod = null;
            }

            loaded = true;
        } catch (Exception e) {
            clearReflection();
        }
    }

    private static void clearReflection() {
        loaded = false;
        playerThirstField = null;
        drinkMethod = null;
        getPurityMethod = null;
        getPurityTextMethod = null;
        getPurityColorMethod = null;
        addPurityByBlockMethod = null;
        addPurityByValueMethod = null;
        getBlockPurityMethod = null;
        givePurityEffectsMethod = null;
        givePurityEffectsStackMethod = null;
    }

    public static void drink(Player player, int thirst, int quenched) {
        if (!loaded || playerThirstField == null || drinkMethod == null) {
            return;
        }

        try {
            Object capabilityObj = playerThirstField.get(null);
            if (!(capabilityObj instanceof Capability<?> capability)) {
                return;
            }

            LazyOptional<?> optional = player.getCapability(capability);
            optional.ifPresent(cap -> {
                try {
                    drinkMethod.invoke(cap, player, thirst, quenched);
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
    }

    public static TagKey<Fluid> getWaterTag() {
        return FluidTags.WATER;
    }

    public static int getPurity(ItemStack stack) {
        int fallback = getStoredPurity(stack);
        if (!loaded || getPurityMethod == null) {
            return fallback;
        }

        try {
            Object result = getPurityMethod.invoke(null, stack);
            if (result instanceof Number number) {
                return number.intValue();
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    public static Component getPurityComponent(int purity) {
        if (!loaded) {
            return Component.translatable("tooltip.createsurvival.purity_level", purity);
        }

        try {
            if (getPurityTextMethod != null) {
                Object result = getPurityTextMethod.invoke(null, purity);
                if (result != null) {
                    return Component.literal(result.toString());
                }
            }
        } catch (Exception ignored) {}
        return Component.translatable("tooltip.createsurvival.purity_level", purity);
    }

    public static int getPurityColor(int purity) {
        if (!loaded || getPurityColorMethod == null) {
            return switch (Math.max(0, purity)) {
                case 0 -> 0x6B6B6B;
                case 1 -> 0x7A4C20;
                case 2 -> 0x4D8A9B;
                default -> 0x3BC6FF;
            };
        }

        try {
            Object result = getPurityColorMethod.invoke(null, purity);
            if (result instanceof Number number) {
                return number.intValue();
            }
        } catch (Exception ignored) {}
        return 0xFFFFFF;
    }

    public static boolean applyPurityEffects(Player player, ItemStack stack) {
        if (!loaded) {
            return true;
        }

        try {
            if (givePurityEffectsStackMethod != null) {
                Object result = givePurityEffectsStackMethod.invoke(null, player, stack);
                if (result instanceof Boolean booleanResult) {
                    return booleanResult;
                }
            }
        } catch (Exception ignored) {}

        return applyPurityEffects(player, getPurity(stack));
    }

    public static boolean applyPurityEffects(Player player, int purity) {
        if (!loaded || givePurityEffectsMethod == null) {
            return true;
        }

        try {
            Object result = givePurityEffectsMethod.invoke(null, player, purity);
            if (result instanceof Boolean booleanResult) {
                return booleanResult;
            }
        } catch (Exception ignored) {}

        return true;
    }

    public static void applyBlockPurity(ItemStack stack, Level level, BlockPos pos) {
        if (!loaded || getBlockPurityMethod == null) {
            setPurity(stack, estimatePurity(level, pos));
            return;
        }

        try {
            if (addPurityByBlockMethod != null) {
                addPurityByBlockMethod.invoke(null, stack, pos, level);
                return;
            }

            Object result = getBlockPurityMethod.invoke(null, level, pos);
            if (result instanceof Number number && addPurityByValueMethod != null) {
                addPurityByValueMethod.invoke(null, stack, number.intValue());
            }
        } catch (Exception ignored) {}
    }

    public static void setPurity(ItemStack stack, int purity) {
        if (stack.isEmpty()) {
            return;
        }

        if (loaded && addPurityByValueMethod != null) {
            try {
                addPurityByValueMethod.invoke(null, stack, purity);
            } catch (Exception ignored) {}
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (purity <= 0) {
            tag.remove("Purity");
        } else {
            tag.putInt("Purity", purity);
        }
    }

    private static int getStoredPurity(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Purity")) {
            return tag.getInt("Purity");
        }
        return 0;
    }

    private static int estimatePurity(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return 0;
        }
        var fluidState = level.getFluidState(pos);
        if (fluidState.is(FluidTags.WATER)) {
            return fluidState.isSource() ? 3 : 2;
        }
        return 0;
    }

    public static void appendPurityTooltip(ItemStack stack, List<Component> tooltip) {
        if (tooltip == null) {
            return;
        }

        if (!loaded) {
            return;
        }

        int purity = getPurity(stack);
        Component purityText = getPurityComponent(purity);
        int color = getPurityColor(purity) & 0xFFFFFF;
        MutableComponent component = Component.literal("Purity: ").append(purityText.copy());
        tooltip.add(component.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
    }
}
