package com.cixxyt.createsurvival.registry;

import com.cixxyt.createsurvival.CreateSurvivalMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry hub for all custom creative tabs owned by Create: Survival.
 * <p>
 * The {@link DeferredRegister} mirrors the pattern we use for items, which keeps tab
 * construction nice and lazy until Forge is ready to build the creative inventory screen.
 */
@Mod.EventBusSubscriber(modid = CreateSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    /**
     * Forge exposes the creative tab registry via {@link Registries#CREATIVE_MODE_TAB}.  By
     * pairing it with our mod id we receive a dedicated registration namespace just like we do
     * for items and blocks.
     */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateSurvivalMod.MODID);

    /**
     * Resource keys act like the tab's Social Security number: they uniquely identify the entry
     * inside the registry and let other systems perform lookups without needing a direct
     * reference to the {@link CreativeModeTab} instance.
     */
    public static final ResourceKey<CreativeModeTab> CREATE_SURVIVAL_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            new ResourceLocation(CreateSurvivalMod.MODID, "create_survival"));

    /**
     * The {@link RegistryObject} wraps the builder that used to live in {@code CreateSurvivalTab}.
     * Forge executes the supplier once the tab registry is ready, after which calls to
     * {@link RegistryObject#get()} are safe anywhere in code.
     */
    public static final RegistryObject<CreativeModeTab> CREATE_SURVIVAL_TAB =
            CREATIVE_TABS.register("create_survival", () -> CreativeModeTab.builder()
                    // The icon uses the clockwork canteen because it is thematically central to
                    // the mod and instantly communicates the tab's content focus to players.
                    .icon(() -> new ItemStack(ModItems.CLOCKWORK_CANTEEN.get()))
                    .title(Component.literal("Create Survival"))
                    // This lambda fires whenever Forge needs the tab's contents.  We deliberately
                    // call {@code get()} on each {@link RegistryObject} so item construction stays
                    // synchronized with registry readiness.
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.CLOCKWORK_CANTEEN.get());
                        output.accept(ModItems.STEAM_RATION.get());
                        output.accept(ModItems.STEAM_CANISTER.get());
                        output.accept(ModItems.SURVEYOR_THERMOMETER.get());
                        output.accept(ModItems.THERMO_VEST.get());
                        output.accept(ModBlocks.GYROTHERMAL_REGULATOR.get());
                        output.accept(ModBlocks.MECHANICAL_LAMP.get());
                    })
                    .build());

    /**
     * Hooked from the mod constructor so Forge can queue up our creative tab registrations.
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
