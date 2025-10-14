//package com.cixxyt.createsurvival.registry;
//
//import com.cixxyt.createsurvival.CreateSurvivalMod;
//import net.minecraft.world.item.CreativeModeTabs;
//import net.minecraft.world.item.Item;
//import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//import net.minecraftforge.registries.DeferredRegister;
//import net.minecraftforge.registries.ForgeRegistries;
//import net.minecraftforge.registries.RegistryObject;
//
//public class ModItems {
//
//    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CreateSurvivalMod.MODID);
//
//    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item",
//            () -> new Item(new Item.Properties().tab(CreativeModeTabs.TOOLS_AND_UTILITIES)));
//
//    public static void register() {
//        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
//    }
//}
