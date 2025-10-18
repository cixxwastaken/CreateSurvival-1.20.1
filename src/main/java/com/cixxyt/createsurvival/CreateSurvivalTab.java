package com.cixxyt.createsurvival;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import com.cixxyt.createsurvival.registry.ModItems;
import net.minecraft.network.chat.Component;

public class CreateSurvivalTab {

    public static final CreativeModeTab TAB = CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.TEST_ITEM.get()))
            .title(Component.literal("Create Survival"))
           .build();
}