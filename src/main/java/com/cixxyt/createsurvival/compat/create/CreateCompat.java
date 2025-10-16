package com.cixxyt.createsurvival.compat.create;

import com.cixxyt.createsurvival.content.items.FlaskItem;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class CreateCompat {
    public static void registerIntegration(FMLCommonSetupEvent event) {
//        event.enqueueWork(() -> {
//            // Register the flask with GenericItemFilling
//            GenericItemFilling.registerItemFiller(
//                    (level, stack) -> stack.getItem() instanceof FlaskItem,
//                    (level, stack, fluidStack) -> ((FlaskItem) stack.getItem()).fillFromSpout(level, stack, fluidStack),
//                    (level, stack, fluidStack) -> ((FlaskItem) stack.getItem()).getRequiredAmountForFill(level, stack, fluidStack),
//                    (stack) -> ((FlaskItem) stack.getItem()).canBeFilledFromSpout(stack)
//            );
//        });

    }
    public static void registerIntegration(IEventBus eventBus) {
        System.out.println("[CreateSurvival] Cold Sweat detected — enabling crossover features!");

        // Example: register custom items, temperature modifiers, etc.
        // ColdSweatAPI.registerTemperatureEffect(...);
    }
}
