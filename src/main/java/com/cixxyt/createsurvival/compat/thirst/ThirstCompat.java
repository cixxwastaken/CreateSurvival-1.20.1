package com.cixxyt.createsurvival.compat.thirst;

import com.cixxyt.createsurvival.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
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
    private static Class<?> registerThirstValueEventClass;
    private static Method addDrinkRegistrationMethod;
    private static Method addContainerRegistrationMethod;
    private static Constructor<?> containerConstructor;
    private static Method containerCanHarvestMethod;
    private static boolean listenerRegistered;

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

            registerThirstValueEventClass = Class.forName("dev.ghen.thirst.foundation.common.event.RegisterThirstValueEvent");
            Class<?> containerWithPurityClass = Class.forName("dev.ghen.thirst.content.purity.ContainerWithPurity");
            containerConstructor = containerWithPurityClass.getConstructor(ItemStack.class, ItemStack.class);
            try {
                containerCanHarvestMethod = containerWithPurityClass.getMethod("canHarvestRunningWater", boolean.class);
            } catch (NoSuchMethodException ignored) {
                containerCanHarvestMethod = null;
            }
            addDrinkRegistrationMethod = registerThirstValueEventClass.getMethod("addDrink", Item.class, int.class, int.class);
            addContainerRegistrationMethod = registerThirstValueEventClass.getMethod("addContainer", containerWithPurityClass);

            loaded = true;
            registerThirstListener();
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
        registerThirstValueEventClass = null;
        addDrinkRegistrationMethod = null;
        addContainerRegistrationMethod = null;
        containerConstructor = null;
        containerCanHarvestMethod = null;
        listenerRegistered = false;
    }

    private static void registerThirstListener() {
        if (!loaded || listenerRegistered || registerThirstValueEventClass == null) {
            return;
        }

        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, (Class) registerThirstValueEventClass, ThirstCompat::handleRegisterThirstValues);
        listenerRegistered = true;
    }

    private static void handleRegisterThirstValues(Object event) {
        if (!loaded || addDrinkRegistrationMethod == null || addContainerRegistrationMethod == null || containerConstructor == null) {
            return;
        }

        RegistryObject<Item> emptyFlask = ModItems.FLASK;
        RegistryObject<Item> waterFlask = ModItems.FLASK_WATER;
        if (!emptyFlask.isPresent() || !waterFlask.isPresent()) {
            return;
        }

        try {
            addDrinkRegistrationMethod.invoke(event, waterFlask.get(), 2, 1);

            ItemStack emptyStack = new ItemStack(emptyFlask.get());
            ItemStack filledStack = new ItemStack(waterFlask.get());
            Object container = containerConstructor.newInstance(emptyStack, filledStack);
            if (containerCanHarvestMethod != null) {
                containerCanHarvestMethod.invoke(container, Boolean.FALSE);
            }
            addContainerRegistrationMethod.invoke(event, container);
        } catch (Exception ignored) {
        }
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

    public static Component getPurityComponent(int purity) {
        if (!loaded) {
            return Component.literal("Unknown");
        }

        try {
            if (getPurityTextMethod != null) {
                Object result = getPurityTextMethod.invoke(null, purity);
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
