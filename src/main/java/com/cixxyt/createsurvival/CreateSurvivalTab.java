package com.cixxyt.createsurvival;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import com.cixxyt.createsurvival.registry.ModItems;
import net.minecraft.network.chat.Component;

public class CreateSurvivalTab {

    public static final CreativeModeTab TAB = CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.CLOCKWORK_CANTEEN.get()))
            .title(Component.literal("Create Survival"))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.CLOCKWORK_CANTEEN.get());
                output.accept(ModItems.STEAM_RATION.get());
                output.accept(ModItems.MECHANICAL_PURIFIER.get());
                output.accept(ModItems.TEST_ITEM.get());
            })
           .build();
}