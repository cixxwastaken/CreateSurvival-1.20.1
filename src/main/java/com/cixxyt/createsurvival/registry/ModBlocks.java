package com.cixxyt.createsurvival.registry;

import com.cixxyt.createsurvival.CreateSurvivalMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.cixxyt.createsurvival.content.blocks.MechanicalPurifierBlock;

@Mod.EventBusSubscriber(modid = CreateSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CreateSurvivalMod.MODID);

    public static final RegistryObject<Block> MECHANICAL_PURIFIER = BLOCKS.register("mechanical_purifier",
            () -> new MechanicalPurifierBlock(Block.Properties.copy(Blocks.IRON_BLOCK)));

    public static void registerIntegration(IEventBus eventBus) {
        System.out.println("[CreateSurvival] Registering ModBlocks");

        // Example: register custom items, temperature modifiers, etc.
        // ColdSweatAPI.registerTemperatureEffect(...);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        System.out.println("[CreateSurvival] Registering ModItems");
    }
}
