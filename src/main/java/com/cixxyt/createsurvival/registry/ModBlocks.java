package com.cixxyt.createsurvival.registry;

import com.cixxyt.createsurvival.CreateSurvivalMod;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Central location for registering custom blocks.  The registry currently sits empty because
 * the Mechanical Water Purifier was removed in favor of external mods that already cover the
 * feature, but keeping the class in place makes it trivial to reintroduce new blocks later.
 */
@Mod.EventBusSubscriber(modid = CreateSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBlocks {
    private ModBlocks() {}

    /** Deferred register wrapper that lets Forge know this mod may contribute block entries. */
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CreateSurvivalMod.MODID);

    /** Hooked by the mod constructor so Forge can process the block registry. */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
