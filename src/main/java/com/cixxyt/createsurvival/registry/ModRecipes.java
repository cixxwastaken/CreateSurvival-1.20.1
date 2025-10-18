package com.cixxyt.createsurvival.registry;

import com.cixxyt.createsurvival.content.items.ClockworkCanteenItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * Houses Create-specific recipe bridges.  By isolating the reflective Create hooks in a single
 * class we keep the rest of the mod free from optional dependency noise, which is especially handy
 * when the project is opened in a workspace without Create on the classpath.
 */
public class ModRecipes {

    private static final Logger LOGGER = LogManager.getLogger("CreateSurvival|CreateCompat");
    private static final ResourceLocation PURIFIED_WATER_ID = new ResourceLocation("thirst", "purified_water");
    private static final int FILLING_AMOUNT = 1000;

    private ModRecipes() {}

    public static void registerRecipes() {
        // The Create API exposes a helper named GenericItemFilling which accepts item-specific
        // fillers via a registry.  Because Create is an optional dependency we resolve that helper
        // reflectively and install a dynamic proxy that speaks whatever functional interface the
        // current Create build expects.  This approach looks a little involved, but it keeps the
        // code resilient to minor signature shuffles between Create patch versions.
        try {
            Class<?> fillingClass = Class.forName("com.simibubi.create.content.fluids.transfer.GenericItemFilling");
            Method registerMethod = null;
            for (Method method : fillingClass.getMethods()) {
                if (method.getName().equals("registerItemFiller") && method.getParameterCount() == 2) {
                    registerMethod = method;
                    break;
                }
            }
            if (registerMethod == null) {
                return;
            }

            // GenericItemFilling switched parameter ordering once in Create's history, so we detect
            // whether the item comes in as a supplier or the raw Item instance and adapt accordingly.
            Object itemArg;
            Class<?> itemParam = registerMethod.getParameterTypes()[0];
            Item canteen = ModItems.CLOCKWORK_CANTEEN.get();
            if (itemParam.isInstance(ModItems.CLOCKWORK_CANTEEN)) {
                itemArg = ModItems.CLOCKWORK_CANTEEN;
            } else {
                itemArg = canteen;
            }

            Class<?> fillerInterface = registerMethod.getParameterTypes()[1];
            Object fillerProxy = Proxy.newProxyInstance(
                    fillerInterface.getClassLoader(),
                    new Class<?>[]{fillerInterface},
                    buildCanteenFillerHandler(canteen));

            registerMethod.invoke(null, itemArg, fillerProxy);
        } catch (ClassNotFoundException ignored) {
            // Create is not present; optional integration silently disables itself.
        } catch (Exception exception) {
            // Any other failure should be loud enough for a log file while keeping the game running.
            LOGGER.warn("Create item filling integration failed to initialize", exception);
        }
    }

    private static InvocationHandler buildCanteenFillerHandler(Item canteen) {
        return (proxy, method, args) -> {
            // The proxy can receive calls such as canFill, getRequiredAmount, and fill.  We branch on
            // the return type so that the handler adapts to Create's evolving functional interface
            // names without losing readability.
            Class<?> returnType = method.getReturnType();
            if (returnType == boolean.class || returnType == Boolean.class) {
                Level level = extractArg(args, Level.class);
                ItemStack stack = extractArg(args, ItemStack.class);
                FluidStack fluid = extractArg(args, FluidStack.class);
                return canFill(level, stack, fluid, canteen);
            }
            if (returnType == int.class || returnType == Integer.class) {
                FluidStack fluid = extractArg(args, FluidStack.class);
                return getRequiredAmount(fluid);
            }
            if (ItemStack.class.isAssignableFrom(returnType)) {
                Level level = extractArg(args, Level.class);
                BlockPos pos = extractArg(args, BlockPos.class);
                ItemStack stack = extractArg(args, ItemStack.class);
                FluidStack fluid = extractArg(args, FluidStack.class);
                boolean simulate = extractBooleanArg(args);
                return fill(level, pos, stack, fluid, simulate, canteen);
            }

            // For any ancillary helper we defer to default behaviour so equals/hashCode keep working.
            if (method.getDeclaringClass() == Object.class) {
                return handleObjectMethod(proxy, method, args);
            }
            return null;
        };
    }

    private static boolean canFill(Level level, ItemStack stack, FluidStack fluid, Item canteen) {
        if (stack == null || stack.isEmpty() || !Objects.equals(stack.getItem(), canteen)) {
            return false;
        }
        if (ClockworkCanteenItem.isFilled(stack)) {
            return false;
        }
        return isPurifiedWater(fluid);
    }

    private static int getRequiredAmount(FluidStack fluid) {
        return isPurifiedWater(fluid) ? FILLING_AMOUNT : 0;
    }

    private static ItemStack fill(Level level, BlockPos pos, ItemStack stack, FluidStack fluid, boolean simulate, Item canteen) {
        if (!canFill(level, stack, fluid, canteen)) {
            return ItemStack.EMPTY;
        }

        ItemStack filled = stack.copy();
        filled.setCount(1);

        if (simulate) {
            // Create calls the filler in simulate mode while probing recipe viability.  We still
            // build the preview stack below, but touching the flag keeps the intent explicit for
            // anyone reading the proxy.
        }

        // We call the existing item logic so the Create automation behaves identically to a player
        // right-clicking a water source.  The block position provided by Create usually points at the
        // spout or basin, but the purity metadata is overridden below using the fluid stack so the
        // exact block state becomes irrelevant.
        if (level != null) {
            BlockPos fillPos = pos != null ? pos : BlockPos.ZERO;
            ClockworkCanteenItem.fillFromSource(filled, level, fillPos);
        } else {
            ClockworkCanteenItem.setSips(filled, ClockworkCanteenItem.MAX_SIPS);
        }

        int purity = Math.max(1, Math.min(ClockworkCanteenItem.MAX_PURITY, getPurityFromFluid(fluid)));
        ClockworkCanteenItem.setPurity(filled, purity);
        return filled;
    }

    private static boolean isPurifiedWater(FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return false;
        }

        Fluid target = ForgeRegistries.FLUIDS.getValue(PURIFIED_WATER_ID);
        if (target != null && fluid.getFluid().isSame(target)) {
            return true;
        }

        // The new thirst manager still honors any fluid that advertises a positive purity value so
        // datapacks can experiment with Create automation without touching Java code.
        if (fluid.getFluid().isSame(Fluids.WATER)) {
            return getPurityFromFluid(fluid) > 0;
        }
        return false;
    }

    private static int getPurityFromFluid(FluidStack fluid) {
        if (fluid == null || !fluid.hasTag()) {
            return ClockworkCanteenItem.MAX_PURITY;
        }
        if (fluid.getTag() != null && fluid.getTag().contains("Purity")) {
            return fluid.getTag().getInt("Purity");
        }
        return ClockworkCanteenItem.MAX_PURITY;
    }

    private static <T> T extractArg(Object[] args, Class<T> type) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (type.isInstance(arg)) {
                return type.cast(arg);
            }
        }
        return null;
    }

    private static boolean extractBooleanArg(Object[] args) {
        if (args == null) {
            return false;
        }
        for (Object arg : args) {
            if (arg instanceof Boolean bool) {
                return bool;
            }
        }
        return false;
    }

    private static Object handleObjectMethod(Object proxy, Method method, Object[] args) throws Exception {
        String name = method.getName();
        return switch (name) {
            case "toString" -> "ClockworkCanteenFillerProxy";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> method.invoke(proxy, args);
        };
    }
}
