package com.cixxyt.createsurvival.compat.thirst;

import net.minecraft.core.BlockPos;
//<<<<<<< codex/fix-crashing-errors-related-to-tooltip-828nej
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
//=======
//>>>>>>> master
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
    private static Method addPurityMethod;
    private static Method getBlockPurityMethod;
    private static Method givePurityEffectsMethod;
//<<<<<<< codex/fix-crashing-errors-related-to-tooltip-828nej
    private static Method appendPurityTooltipMethod;
//=======
//>>>>>>> master

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
//<<<<<<< codex/fix-crashing-errors-related-to-tooltip-828nej
            try {
                getPurityTextMethod = waterPurityClass.getMethod("getPurityText", int.class);
            } catch (NoSuchMethodException ignored) {
                getPurityTextMethod = null;
            }
//=======
            getPurityTextMethod = waterPurityClass.getMethod("getPurityText", int.class);
//>>>>>>> master
            getPurityColorMethod = waterPurityClass.getMethod("getPurityColor", int.class);
            addPurityMethod = waterPurityClass.getMethod("addPurity", ItemStack.class, int.class);
            getBlockPurityMethod = waterPurityClass.getMethod("getBlockPurity", Level.class, BlockPos.class);
            givePurityEffectsMethod = waterPurityClass.getMethod("givePurityEffects", Player.class, int.class);

//<<<<<<< codex/fix-crashing-errors-related-to-tooltip-828nej
            try {
                appendPurityTooltipMethod = waterPurityClass.getMethod("appendTooltip", ItemStack.class, List.class);
            } catch (NoSuchMethodException ignored) {
                appendPurityTooltipMethod = null;
            }

//=======
//>>>>>>> master
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
        addPurityMethod = null;
        getBlockPurityMethod = null;
        givePurityEffectsMethod = null;
//<<<<<<< codex/fix-crashing-errors-related-to-tooltip-828nej
        appendPurityTooltipMethod = null;
//=======
//>>>>>>> master
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

    public static int getPurity(ItemStack stack) {
        if (!loaded || getPurityMethod == null) {
            return 0;
        }

        try {
            Object result = getPurityMethod.invoke(null, stack);
            if (result instanceof Number number) {
                return number.intValue();
            }
        } catch (Exception ignored) {}
        return 0;
    }

//<<<<<<< codex/fix-crashing-errors-related-to-tooltip-828nej
    public static Component getPurityComponent(int purity) {
        if (!loaded) {
            return Component.literal("Unknown");
        }

        try {
            if (getPurityTextMethod != null) {
                Object result = getPurityTextMethod.invoke(null, purity);
                if (result instanceof Component component) {
                    return component;
                }
                if (result != null) {
                    return Component.literal(result.toString());
                }
            }
        } catch (Exception ignored) {}
        return Component.literal("Unknown");
    }

    public static int getPurityColor(int purity) {
        if (!loaded || getPurityColorMethod == null) {
            return 0xFFFFFF;
        }

        try {
            Object result = getPurityColorMethod.invoke(null, purity);
            if (result instanceof Number number) {
                return number.intValue();
            }
        } catch (Exception ignored) {}
        return 0xFFFFFF;
    }

    public static void givePurityEffects(Player player, int purity) {
        if (!loaded || givePurityEffectsMethod == null) {
            return;
        }

        try {
            givePurityEffectsMethod.invoke(null, player, purity);
        } catch (Exception ignored) {}
    }

    public static void applyBlockPurity(ItemStack stack, Level level, BlockPos pos) {
        if (!loaded || addPurityMethod == null || getBlockPurityMethod == null) {
            return;
        }

        try {
//=======
    public static String getPurityText(int purity) {
        if (!loaded || getPurityTextMethod == null) {
            return "Unknown";
        }

        try {
            Object result = getPurityTextMethod.invoke(null, purity);
            if (result != null) {
                return result.toString();
            }
        } catch (Exception ignored) {}
        return "Unknown";
    }

    public static int getPurityColor(int purity) {
        if (!loaded || getPurityColorMethod == null) {
            return 0xFFFFFF;
        }

        try {
            Object result = getPurityColorMethod.invoke(null, purity);
            if (result instanceof Number number) {
                return number.intValue();
            }
        } catch (Exception ignored) {}
        return 0xFFFFFF;
    }

    public static void givePurityEffects(Player player, int purity) {
        if (!loaded || givePurityEffectsMethod == null) {
            return;
        }

        try {
            givePurityEffectsMethod.invoke(null, player, purity);
        } catch (Exception ignored) {}
    }

    public static void applyBlockPurity(ItemStack stack, Level level, BlockPos pos) {
        if (!loaded || addPurityMethod == null || getBlockPurityMethod == null) {
            return;
        }

        try {
//>>>>>>> master
            Object result = getBlockPurityMethod.invoke(null, level, pos);
            if (result instanceof Number number) {
                addPurityMethod.invoke(null, stack, number.intValue());
            }
        } catch (Exception ignored) {}
    }

    public static void appendPurityTooltip(ItemStack stack, List<Component> tooltip) {
        if (tooltip == null) {
            return;
        }

        if (!loaded) {
            return;
        }

        try {
            if (appendPurityTooltipMethod != null) {
                appendPurityTooltipMethod.invoke(null, stack, tooltip);
                return;
            }
        } catch (Exception ignored) {
            // Fallback to manual tooltip below
        }

        int purity = getPurity(stack);
        Component purityText = getPurityComponent(purity);
        int color = getPurityColor(purity) & 0xFFFFFF;
        MutableComponent component = Component.literal("Purity: ").append(purityText.copy());
        tooltip.add(component.withStyle(style -> style.withColor(TextColor.fromRgb(color))));
    }
}
