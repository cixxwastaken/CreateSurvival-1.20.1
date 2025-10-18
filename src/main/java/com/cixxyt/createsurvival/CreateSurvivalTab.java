package com.cixxyt.createsurvival;

import com.cixxyt.createsurvival.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Helper that defines the dedicated creative tab for Create: Survival goodies.
 */
public final class CreateSurvivalTab {
    private CreateSurvivalTab() {}

    public static final CreativeModeTab TAB = CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.CLOCKWORK_CANTEEN.get()))
            .title(Component.literal("Create Survival"))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.CLOCKWORK_CANTEEN.get());
                output.accept(ModItems.STEAM_RATION.get());
                output.accept(ModItems.STEAM_CANISTER.get());
                output.accept(ModItems.SURVEYOR_THERMOMETER.get());
                output.accept(ModItems.THERMO_VEST.get());
            })
            .build();
}
