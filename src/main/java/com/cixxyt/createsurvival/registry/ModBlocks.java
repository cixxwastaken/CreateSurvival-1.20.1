package com.cixxyt.createsurvival.registry;

import com.cixxyt.createsurvival.CreateSurvivalMod;
import com.cixxyt.createsurvival.content.blocks.MechanicalLampBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Central location for registering custom blocks.  Each {@link RegistryObject} acts like a lazy
 * supplier that Forge wires into the block registry when the mod loads, which keeps initialization
 * order predictable even as the codebase grows.
 */
@Mod.EventBusSubscriber(modid = CreateSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBlocks {
    private ModBlocks() {}

    /** Deferred register wrapper that lets Forge know this mod may contribute block entries. */
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CreateSurvivalMod.MODID);

    /**
     * A kinetic lamp that brightens with the delivered rotational speed.  The verbose comment is
     * intentional: understanding that the block is a passive consumer helps newcomers reason about
     * stress propagation when they start stringing Create machines together.
     */
    public static final RegistryObject<Block> MECHANICAL_LAMP = BLOCKS.register("mechanical_lamp",
            () -> new MechanicalLampBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.LANTERN)));

    /** Hooked by the mod constructor so Forge can process the block registry. */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
