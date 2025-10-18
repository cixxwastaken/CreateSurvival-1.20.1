package com.cixxyt.createsurvival.registry;

import com.cixxyt.createsurvival.CreateSurvivalMod;
import com.cixxyt.createsurvival.content.items.ClockworkCanteenItem;
import com.cixxyt.createsurvival.content.items.SteamCanisterItem;
import com.cixxyt.createsurvival.content.items.SteamRationItem;
import com.cixxyt.createsurvival.content.items.SurveyorThermometerItem;
import com.cixxyt.createsurvival.content.items.ThermoVestItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Master item registry.  Each {@link RegistryObject} lazily constructs the item when Forge asks
 * for it, so it is safe to reference these suppliers anywhere once registration has completed.
 */
@Mod.EventBusSubscriber(modid = CreateSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreateSurvivalMod.MODID);

    public static final RegistryObject<Item> CLOCKWORK_CANTEEN = ITEMS.register("clockwork_canteen",
            () -> new ClockworkCanteenItem(new Item.Properties()));

    public static final RegistryObject<Item> STEAM_RATION = ITEMS.register("steam_ration",
            () -> new SteamRationItem(new Item.Properties()));

    public static final RegistryObject<Item> STEAM_CANISTER = ITEMS.register("steam_canister",
            () -> new SteamCanisterItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SURVEYOR_THERMOMETER = ITEMS.register("surveyor_thermometer",
            () -> new SurveyorThermometerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> THERMO_VEST = ITEMS.register("thermo_vest",
            () -> new ThermoVestItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
