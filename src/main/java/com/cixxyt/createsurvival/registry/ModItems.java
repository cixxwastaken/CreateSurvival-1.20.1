package com.cixxyt.createsurvival.registry;

import com.cixxyt.createsurvival.CreateSurvivalMod;
import com.cixxyt.createsurvival.content.items.ClockworkCanteenItem;
import com.cixxyt.createsurvival.content.items.SteamRationItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import com.cixxyt.createsurvival.registry.ModBlocks;

@Mod.EventBusSubscriber(modid = CreateSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreateSurvivalMod.MODID);

    public static final RegistryObject<Item> TEST_ITEM = ITEMS.register("test_item",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CLOCKWORK_CANTEEN = ITEMS.register("clockwork_canteen",
            () -> new ClockworkCanteenItem(new Item.Properties()));

    public static final RegistryObject<Item> STEAM_RATION = ITEMS.register("steam_ration",
            () -> new SteamRationItem(new Item.Properties()));

    public static final RegistryObject<Item> MECHANICAL_PURIFIER = ITEMS.register("mechanical_purifier",
            () -> new BlockItem(ModBlocks.MECHANICAL_PURIFIER.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        System.out.println("[CreateSurvival] Registering ModItems");
    }
}

