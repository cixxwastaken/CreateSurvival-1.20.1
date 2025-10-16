package com.cixxyt.createsurvival.registry;

import com.cixxyt.createsurvival.content.items.FlaskItem;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import net.minecraft.world.level.material.Fluids;

public class ModRecipes {

    public static void registerRecipes() {

        // Register the FlaskItem as fillable for Create spouts
//        GenericItemFilling.registerItemFiller(FlaskItem.class, (stack, fluid) -> {
//            return FlaskItem.getStoredFluid(stack) == Fluids.EMPTY;
//        });

    }
}
